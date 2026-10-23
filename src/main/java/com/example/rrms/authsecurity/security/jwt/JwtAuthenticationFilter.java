package com.example.rrms.authsecurity.security.jwt;

import com.example.rrms.authsecurity.security.user.CustomUserDetailsService;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import com.example.rrms.common.exception.ApiErrorWriter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/login") ||
               path.contains("/mfa/verify") ||
               path.contains("/refresh") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        while (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        try {
            Claims claims = jwtService.parse(token);

            // 1. Only Access tokens authenticate API calls
            if (!JwtService.PURPOSE_ACCESS.equals(claims.get("purpose", String.class))) {
                request.setAttribute(ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_WRONG_PURPOSE");
                throw new JwtException("Wrong token purpose");
            }

            // 2. Load the current user state from DB
            UserPrincipal userPrincipal = customUserDetailsService.loadUserById(UUID.fromString(claims.getSubject()));

            // 3. Cross-check token vs DB
            if (!userPrincipal.isEnabled() || !userPrincipal.isAccountNonLocked()) {
                request.setAttribute(ApiErrorWriter.AUTH_ERROR_ATTR, "ACCOUNT_DISABLED");
                throw new JwtException("Account disabled, tenant suspended, or account locked");
            }

            UUID tokenTenant = JwtService.uuidClaim(claims, "tenantId");
            if (!Objects.equals(tokenTenant, userPrincipal.getTenantId())) {
                request.setAttribute(ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_TENANT_MISMATCH");
                throw new JwtException("Tenant mismatch");
            }

            Integer version = JwtService.intClaim(claims, "tokenVersion");
            if (version == null || version != userPrincipal.getTokenVersion()) {
                request.setAttribute(ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_REVOKED");
                throw new JwtException("Token revoked");
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities()));

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            request.setAttribute(ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_EXPIRED");
            SecurityContextHolder.clearContext();
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            if (request.getAttribute(ApiErrorWriter.AUTH_ERROR_ATTR) == null) {
                request.setAttribute(ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_INVALID");
            }
            SecurityContextHolder.clearContext();   // leave unauthenticated -> entry point returns 401
        }

        chain.doFilter(request, response);
    }
}
