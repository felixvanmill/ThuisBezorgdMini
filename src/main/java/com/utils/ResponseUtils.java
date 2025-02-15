package com.utils;

import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for handling API responses consistently.
 */
public class ResponseUtils {

    /**
     * Handles service calls and wraps exceptions in a consistent error response.
     */
    public static <T> ResponseEntity<T> handleRequest(Supplier<T> action) {
        try {
            return ResponseEntity.ok(action.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Internal server error"));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createErrorResponse(String message) {
        return (T) Map.of("error", message);
    }
}
