package com.utils;

import com.model.CustomerOrder;
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

    /**
     * Validates whether the logged-in delivery person is authorized to manage the given order.
     */
    public static void validateDeliveryPerson(CustomerOrder order, String loggedInUsername) {
        if (!loggedInUsername.equals(order.getDeliveryPerson())) {
            throw new RuntimeException("Unauthorized: You are not assigned to this order.");
        }
    }
}
