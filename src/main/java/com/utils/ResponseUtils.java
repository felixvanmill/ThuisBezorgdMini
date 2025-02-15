package com.utils;

import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.function.Supplier;

public class ResponseUtils {

    /**
     * Handles service calls and wraps exceptions in a consistent error response.
     */
    public static <T> ResponseEntity<?> handleRequest(Supplier<T> action) {
        try {
            return ResponseEntity.ok(action.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}
