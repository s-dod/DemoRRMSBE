
package com.example.rrms.common.exception;

import com.example.rrms.common.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiErrorWriter {

    public static final String AUTH_ERROR_ATTR = "rrms.auth.error";

    private static final Map<String, String> MESSAGES = Map.of(
            "TOKEN_EXPIRED",
            "Access token has expired. Call POST /api/auth/refresh or log in again.",

            "TOKEN_INVALID",
            "Access token is invalid or malformed.",

            "TOKEN_REVOKED",
            "Token is no longer valid (logout, password change or role change). Log in again.",

            "TOKEN_WRONG_PURPOSE",
            "This token cannot call the API. An MFA token only works on /api/auth/mfa/setup and /api/auth/mfa/verify.",

            "TOKEN_TENANT_MISMATCH",
            "Token does not match the user's resort. Log in again.",

            "ACCOUNT_DISABLED",
            "Account is inactive or locked, or the resort is suspended."
    );

    private final ObjectMapper mapper;

    public void write(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            HttpStatus httpStatus,
            String code,
            String message
    ) throws IOException {

        httpServletResponse.setStatus(httpStatus.value());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.setCharacterEncoding("UTF-8");

        ApiResponse<Void> response =
                new ApiResponse<>(
                        false,
                        message,
                        null
                );

        mapper.writeValue(httpServletResponse.getOutputStream(), response);
    }

    /**
     * 401: Uses the reason stored by the JWT filter,
     * otherwise returns an authentication-required message.
     */
    public void unauthorized(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) throws IOException {

        String code = (String) httpServletRequest.getAttribute(AUTH_ERROR_ATTR);

        if (code == null) {
            write(
                    httpServletRequest,
                    httpServletResponse,
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Authentication required. Send the Authorization: Bearer header."
            );
        } else {
            write(
                    httpServletRequest,
                    httpServletResponse,
                    HttpStatus.UNAUTHORIZED,
                    code,
                    MESSAGES.getOrDefault(code, "Authentication failed.")
            );
        }
    }

    /**
     * 403: Valid authentication, but insufficient permission.
     */
    public void forbidden(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) throws IOException {

        write(
                httpServletRequest,
                httpServletResponse,
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "You do not have permission to access this resource."
        );
    }
}
