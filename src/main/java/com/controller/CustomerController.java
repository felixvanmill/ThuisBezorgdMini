package com.controller;

import com.dto.CustomerOrderDTO;
import com.dto.OrderDTO;
import com.service.CustomerService;
import com.service.OrderService;
import com.utils.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.utils.AuthUtils;

import java.util.List;
import java.util.Map;


/**
 * Handles customer-related operations, including viewing restaurant menus,
 * placing orders, tracking orders, and canceling orders.
 */
@RestController
@RequestMapping("/api/v1") // Cleaned-up base URL for RESTful API versioning
public class CustomerController {
    private final OrderService orderService;

    private final CustomerService customerService;

    /**
     * Constructor-based Dependency Injection for CustomerService.
     */
    public CustomerController(CustomerService customerService, OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }



    /**
     * Retrieves the menu of a restaurant by its slug.
     */
    @GetMapping("/restaurants/{slug}/menu")
    public ResponseEntity<List<?>> getMenuByRestaurant(@PathVariable String slug) {
        return ResponseEntity.ok(customerService.getMenuByRestaurantSlug(slug));
    }

    /**
     * Places an order for a specific restaurant.
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/restaurants/{slug}/orders")
    public ResponseEntity<OrderDTO> submitOrder(
            @PathVariable String slug,
            @RequestBody List<Map<String, Object>> orderItems) {
        return ResponseEntity.ok(customerService.submitOrder(slug, orderItems));
    }



    /**
     * Retrieves all orders for the authenticated customer.
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/orders")
    public ResponseEntity<List<CustomerOrderDTO>> getAllOrdersForUser() {
        return ResponseEntity.ok(customerService.getAllOrdersForAuthenticatedUser());
    }



    /**
     * Allows customers to cancel their orders (Only "CANCELED" status is allowed).
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PatchMapping("/orders/{orderNumber}/status")
    public ResponseEntity<Map<String, String>> updateOrderStatus(
            @PathVariable String orderNumber,
            @RequestBody Map<String, String> requestBody) {
        return customerService.updateOrderStatus(orderNumber, requestBody.get("status"));
    }



    /**
     * Retrieves a specific order for the authenticated user.
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/orders/{identifier}")
    public ResponseEntity<CustomerOrderDTO> getOrderByIdentifier(@PathVariable String identifier) {
        return customerService.getOrderByIdentifier(AuthUtils.getLoggedInUsername(), identifier);
    }

}
