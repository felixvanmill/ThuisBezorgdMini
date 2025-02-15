package com.utils;

import com.model.CustomerOrder;
import com.model.OrderStatus;

import java.util.Map;

/**
 * Utility class for validation logic.
 */
public class ValidationUtils {

    private ValidationUtils() {} // Prevent instantiation

    /**
     * Validates if the given key exists and is not empty in the request body.
     */
    public static boolean isValidStatus(Map<String, String> requestBody, String key) {
        return requestBody.containsKey(key) && requestBody.get(key) != null && !requestBody.get(key).isBlank();
    }

    /**
     * Extracts and validates the status field.
     */
    public static String extractAndValidateStatus(Map<String, String> requestBody) {
        if (!isValidStatus(requestBody, "status")) {
            throw new IllegalArgumentException("Status must be provided and cannot be empty.");
        }
        return requestBody.get("status");
    }

    /**
     * Parses and validates the order status.
     */
    public static OrderStatus parseOrderStatus(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status provided: " + status);
        }
    }

    /**
     * Ensures the status transition is valid.
     */
    public static void ensureValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
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

    /**
     * Validates whether the logged-in delivery person is authorized to manage the given order.
     */
    public static void validateDeliveryPerson(CustomerOrder order, String loggedInUsername) {
        if (!loggedInUsername.equals(order.getDeliveryPerson())) {
            throw new RuntimeException("Unauthorized: You are not assigned to this order.");
        }
    }
}
