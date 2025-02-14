package com.utils;

import java.util.Map;

public class ExceptionUtils {

    private ExceptionUtils() {} // Private constructor to prevent instantiation

    public static Map<String, String> createErrorMessage(String message) {
        return Map.of("error", message);
    }
}
