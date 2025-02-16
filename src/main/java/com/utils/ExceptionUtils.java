package com.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ExceptionUtils {

    private ExceptionUtils() {} // Private constructor to prevent instantiation

    public static Map<String, String> createErrorMessage(String message) {
        return Map.of("error", message);
    }

    public static ResponseEntity<Map<String, String>> createErrorResponse(int statusCode, String message) {
        return ResponseEntity.status(HttpStatus.valueOf(statusCode))
                .body(createErrorMessage(message));
    }

}
