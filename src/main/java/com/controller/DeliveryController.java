package com.controller;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
import com.service.DeliveryService;
import com.utils.ResponseUtils;
import com.utils.ValidationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles delivery-related operations such as viewing, managing, and confirming orders.
 */
@RestController
@RequestMapping("/api/v1/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * Constructor-based Dependency Injection for DeliveryService.
     */
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Retrieves all orders relevant to delivery personnel.
     */
    @GetMapping("/orders")
    public ResponseEntity<Map<String, List<CustomerOrderDTO>>> getAllOrders() {
        return ResponseEntity.ok(Map.of("orders", deliveryService.getAllDeliveryOrders()));
    }

    /**
     * Assigns the logged-in delivery person to an order.
     */
    @PostMapping("/orders/{identifier}/assign")
    public ResponseEntity<Map<String, Object>> assignDeliveryPerson(@PathVariable String identifier) {
        return ResponseUtils.handleRequest(() -> {
            CustomerOrder order = deliveryService.assignOrder(identifier);
            return Map.of(
                    "message", "Delivery person assigned successfully.",
                    "orderId", order.getId()
            );
        });
    }


    /**
     * Retrieves orders assigned to the logged-in delivery person.
     */
    @GetMapping("/myOrders")
    public ResponseEntity<Map<String, List<CustomerOrderDTO>>> getAssignedOrders() {
        return ResponseEntity.ok(Map.of("orders", deliveryService.getAssignedOrders()));
    }

    /**
     * Retrieves the delivery history for the logged-in delivery person.
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, List<CustomerOrderDTO>>> getDeliveryHistory() {
        return ResponseEntity.ok(Map.of("deliveredOrders", deliveryService.getDeliveryHistory()));
    }

    /**
     * Retrieves details of a specific order.
     */
    @GetMapping("/orders/{identifier}/details")
    public ResponseEntity<CustomerOrderDTO> getOrderDetails(@PathVariable String identifier) {
        return ResponseUtils.handleRequest(() -> deliveryService.getOrderDetails(identifier));
    }

    /**
     * Updates the order status (e.g., Confirm Pickup, Transport, Delivered)
     */
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @PatchMapping("/orders/{identifier}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String identifier,
            @RequestBody Map<String, String> requestBody) {

        return ResponseUtils.handleRequest(() -> {
            String status = requestBody.get("status");

            if (!ValidationUtils.isValidStatus(requestBody, "status")) {
                throw new IllegalArgumentException("Status must be provided.");
            }

            return deliveryService.updateOrderStatus(identifier, status);
        });
    }
}
