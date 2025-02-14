package com.utils;

import java.util.Map;

public class ValidationUtils {

    private ValidationUtils() {} // Private constructor to prevent instantiation

    public static boolean isValidStatus(Map<String, String> requestBody, String key) {
        return requestBody.containsKey(key) && requestBody.get(key) != null && !requestBody.get(key).isBlank();
    }
}
