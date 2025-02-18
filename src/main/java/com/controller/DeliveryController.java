package com.controller;

import com.dto.CustomerOrderDTO;
import com.response.ApiResponse;
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
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<CustomerOrderDTO>>> getAllOrders() {
        List<CustomerOrderDTO> orders = deliveryService.getAllDeliveryOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Assigns the logged-in delivery person to an order.
     */
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @PostMapping("/orders/{identifier}/assign")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignDeliveryPerson(@PathVariable String identifier) {
        return ResponseUtils.handleRequest(() -> ApiResponse.success(deliveryService.assignOrder(identifier)));
    }

    /**
     * Retrieves orders assigned to the logged-in delivery person.
     */
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @GetMapping("/myOrders")
    public ResponseEntity<ApiResponse<List<CustomerOrderDTO>>> getAssignedOrders() {
        List<CustomerOrderDTO> orders = deliveryService.getAssignedOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Retrieves the delivery history for the logged-in delivery person.
     */
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<CustomerOrderDTO>>> getDeliveryHistory() {
        List<CustomerOrderDTO> deliveredOrders = deliveryService.getDeliveryHistory();
        return ResponseEntity.ok(ApiResponse.success(deliveredOrders));
    }

    /**
     * Retrieves details of a specific order.
     */
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @GetMapping("/orders/{identifier}/details")
    public ResponseEntity<ApiResponse<CustomerOrderDTO>> getOrderDetails(@PathVariable String identifier) {
        return ResponseUtils.handleRequest(() -> ApiResponse.success(deliveryService.getOrderDetails(identifier)));
    }

    /**
     * Updates the order status (e.g., Confirm Pickup, Transport, Delivered)
     */
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @PatchMapping("/orders/{identifier}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateOrderStatus(
            @PathVariable String identifier,
            @RequestBody @Valid Map<String, String> requestBody) {

        return ResponseUtils.handleRequest(() -> ApiResponse.success(deliveryService.processOrderStatusUpdate(identifier, requestBody)));
    }

    /**
     * Retrieves the menu items for a specific order along with quantities.
     */
    @PreAuthorize("hasRole('ROLE_DELIVERY_PERSON')")
    @GetMapping("/orders/{identifier}/items")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderItems(@PathVariable String identifier) {
        return ResponseUtils.handleRequest(() -> ApiResponse.success(Map.of(
                "orderItems", deliveryService.getOrderItems(identifier)
        )));
    }
}
