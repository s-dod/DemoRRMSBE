
package com.example.rrms.common.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {

    public static <T> ResponseEntity<ApiResponse<T>> ok(
            String message, T data) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, message, data)
        );
    }

    public static ResponseEntity<ApiResponse<Void>> ok(
            String message) {

        return ResponseEntity.ok(
                new ApiResponse<>(true, message, null)
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(
            String message, T data) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, message, data));
    }

    public static ResponseEntity<ApiResponse<Void>> error(
            HttpStatus status, String message) {

        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, null));
    }
}
