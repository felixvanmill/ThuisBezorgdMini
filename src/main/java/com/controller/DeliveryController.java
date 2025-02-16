package com.controller;

import com.dto.CustomerOrderDTO;
import com.service.DeliveryService;
import com.utils.ResponseUtils;
import jakarta.validation.Valid;
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
        return ResponseUtils.handleRequest(() -> deliveryService.assignOrder(identifier));
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
            @RequestBody @Valid Map<String, String> requestBody) { // ✅ Validatie toegevoegd

        return ResponseUtils.handleRequest(() -> deliveryService.processOrderStatusUpdate(identifier, requestBody));
    }

    /**
     * Retrieves the menu items for a specific order along with quantities.
     */
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @GetMapping("/orders/{identifier}/items")
    public ResponseEntity<Map<String, Object>> getOrderItems(@PathVariable String identifier) {
        return ResponseUtils.handleRequest(() -> Map.of(
                "orderItems", deliveryService.getOrderItems(identifier)
        ));
    }
}
