package com.utils;

import com.model.OrderStatus;

import java.util.Map;

public class ValidationUtils {

    private ValidationUtils() {} // Private constructor to prevent instantiation

    /**
     * Validates if the given key exists and is not empty in the request body.
     */
    public static boolean isValidStatus(Map<String, String> requestBody, String key) {
        return requestBody.containsKey(key) && requestBody.get(key) != null && !requestBody.get(key).isBlank();
    }

    /**
     * Checks if the status transition is valid.
     */
    public static boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case READY_FOR_DELIVERY -> newStatus == OrderStatus.PICKING_UP;
            case PICKING_UP -> newStatus == OrderStatus.TRANSPORT;
            case TRANSPORT -> newStatus == OrderStatus.DELIVERED;
            default -> false;
        };
    }
}
