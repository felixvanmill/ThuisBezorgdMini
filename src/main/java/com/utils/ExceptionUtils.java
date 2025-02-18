package com.utils;

import com.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for building consistent API error responses.
 */
public class ExceptionUtils {

    private ExceptionUtils() {}

    /**
     * Builds a standardized error response.
     * @param status HTTP status.
     * @param message Error message.
     * @return ResponseEntity with structured ApiResponse.
     */
    public static <T> ResponseEntity<ApiResponse<T>> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.failure(message));
    }

    /**
     * Builds a standardized error response with additional data.
     * @param status HTTP status.
     * @param message Error message.
     * @param data Additional error details.
     * @return ResponseEntity with structured ApiResponse.
     */
    public static <T> ResponseEntity<ApiResponse<T>> createErrorResponse(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, data));
    }
}
