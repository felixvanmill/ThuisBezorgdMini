package com.utils;

import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;

import java.util.List;

/**
 * Utility class for order-related operations.
 */
public class OrderUtils {

    private OrderUtils() {} // Prevent instantiation

    /**
     * Retrieves active delivery statuses.
     */
    public static List<OrderStatus> getActiveDeliveryStatuses() {
        return List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.PICKING_UP, OrderStatus.TRANSPORT);
    }

    /**
     * Retrieves statuses for in-progress orders.
     */
    public static List<OrderStatus> getInProgressStatuses() {
        return List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.PICKING_UP, OrderStatus.TRANSPORT);
    }


    /**
     * Finds an order by its identifier.
     */
    public static CustomerOrder findOrderByIdentifier(CustomerOrderRepository repo, String identifier) {
        return identifier.matches("\\d+") ?
                repo.findById(Long.parseLong(identifier))
                        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + identifier))
                : repo.findByOrderNumber(identifier)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
    }

    /**
     * Finds an order with menu items.
     */
    public static CustomerOrder findOrderWithItems(CustomerOrderRepository repo, String identifier) {
        return repo.findByOrderNumberWithItems(identifier)
                .orElseThrow(() -> new RuntimeException("Order not found with identifier: " + identifier));
    }
}
