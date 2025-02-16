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

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {
    private final OrderService orderService;
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService, OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @GetMapping("/restaurants/{slug}/menu")
    public ResponseEntity<List<?>> getMenuByRestaurant(@PathVariable String slug) {
        return ResponseEntity.ok(customerService.getMenuByRestaurantSlug(slug));
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/restaurants/{slug}/orders")
    public ResponseEntity<OrderDTO> submitOrder(
            @PathVariable String slug,
            @RequestBody @Valid List<Map<String, Object>> orderItems) {
        return ResponseEntity.ok(customerService.submitOrder(slug, orderItems));
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/orders")
    public ResponseEntity<List<CustomerOrderDTO>> getAllOrdersForUser() {
        return ResponseEntity.ok(customerService.getAllOrdersForAuthenticatedUser());
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PatchMapping("/orders/{orderNumber}/status")
    public ResponseEntity<Map<String, String>> updateOrderStatus(
            @PathVariable String orderNumber,
            @RequestBody @Valid Map<String, String> requestBody) {
        return customerService.updateOrderStatus(orderNumber, requestBody.get("status"));
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/orders/{identifier}")
    public ResponseEntity<CustomerOrderDTO> getOrderByIdentifier(@PathVariable String identifier) {
        return customerService.getOrderByIdentifier(AuthUtils.getLoggedInUsername(), identifier);
    }
}
