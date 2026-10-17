package com.example.rrms.authsecurity.security.jwt;

import com.example.rrms.authsecurity.config.SecurityProperties;
import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String PURPOSE_ACCESS = "ACCESS";
    public static final String PURPOSE_MFA = "MFA";

    private final SecurityProperties.Jwt props;
    private final SecretKey secretKey;

    public JwtService(SecurityProperties properties) {
        this.props = properties.jwt();
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
    }

    public String generateAccessToken(UserPrincipal userPrincipal) {
        long minutes = userPrincipal.getRole() == Role.GUEST ? props.guestAccessTokenMinutes() : props.accessTokenMinutes();
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(props.issuer())
                .subject(userPrincipal.getId().toString())
                .id(UUID.randomUUID().toString())
                .claim("purpose", PURPOSE_ACCESS)
                .claim("tenantId", userPrincipal.getTenantId() != null ? userPrincipal.getTenantId().toString() : null)                    // null for SUPER_ADMIN
                .claim("role", userPrincipal.getRole().name())
                .claim("staffType", userPrincipal.getStaffType() == null ? null : userPrincipal.getStaffType().name())
                .claim("tokenVersion", userPrincipal.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(minutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public String generateMfaToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(props.issuer())
                .subject(user.getId().toString())
                .claim("purpose", PURPOSE_MFA)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.mfaTokenMinutes(), ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    /** Throws JwtException (expired, bad signature, malformed, wrong issuer). */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static UUID uuidClaim(Claims claims, String name) {
        Object v = claims.get(name);
        if (v == null) return null;
        if (v instanceof UUID u) return u;
        return UUID.fromString(v.toString());
    }

    public static Integer intClaim(Claims claims, String name) {
        Object v = claims.get(name);
        return v == null ? null : ((Number) v).intValue();
    }

    public static Long longClaim(Claims claims, String name) {
        Object v = claims.get(name);
        return v == null ? null : ((Number) v).longValue();
    }
}