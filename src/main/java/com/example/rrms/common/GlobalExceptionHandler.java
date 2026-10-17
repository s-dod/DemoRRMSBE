
package com.example.rrms.common;

import com.example.rrms.common.dto.response.ApiResponse;
import com.example.rrms.common.exception.ApiException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(
            MethodArgumentNotValidException ex,
            HttpServletRequest httpServletRequest) {

        Map<String, String> fields = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError ->
                        fields.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        String firstError = fields.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));

        return error(
                HttpStatus.BAD_REQUEST,
                "Validation failed: " + firstError,
                httpServletRequest
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> unreadable(
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or contains invalid JSON. Check quotes, commas, and enum values.",
                httpServletRequest
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> badParam(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' has an invalid value.",
                httpServletRequest
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> missingParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.BAD_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing.",
                httpServletRequest
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> mediaType(
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type must be application/json.",
                httpServletRequest
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> method(
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method " + httpServletRequest.getMethod()
                        + " is not supported for this URL.",
                httpServletRequest
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> noRoute(
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.NOT_FOUND,
                "No endpoint found for " + httpServletRequest.getMethod()
                        + " " + httpServletRequest.getRequestURI() + ". Check the URL.",
                httpServletRequest
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> badCredentials(
            BadCredentialsException ex,
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password.",
                httpServletRequest
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> locked(
            LockedException ex,
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.LOCKED,
                "Account is locked.",
                httpServletRequest
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> authentication(
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed.",
                httpServletRequest
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> denied(
            AccessDeniedException ex,
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource.",
                httpServletRequest
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> business(
            ApiException ex,
            HttpServletRequest httpServletRequest) {

        return error(ex.getHttpStatus(), ex.getMessage(), httpServletRequest);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(
            EntityNotFoundException ex,
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                httpServletRequest
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> dataConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest httpServletRequest) {

        log.warn(
                "Data integrity violation on {} {}: {}",
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI(),
                ex.getMostSpecificCause().getMessage()
        );

        return error(
                HttpStatus.CONFLICT,
                "A record with the same unique value already exists, or required data is missing.",
                httpServletRequest
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> badArgument(
            IllegalArgumentException ex,
            HttpServletRequest httpServletRequest) {

        return error(
                HttpStatus.BAD_REQUEST,
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Invalid request argument.",
                httpServletRequest
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(
            Exception ex,
            HttpServletRequest httpServletRequest) {

        String ref = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        log.error(
                "Unhandled error [{}] on {} {}",
                ref,
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI(),
                ex
        );

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Reference: " + ref,
                httpServletRequest
        );
    }

    private static ResponseEntity<ApiResponse<Void>> error(
            HttpStatus status,
            String message,
            HttpServletRequest httpServletRequest) {

        return ApiResponse.error(status, message);
    }
}
