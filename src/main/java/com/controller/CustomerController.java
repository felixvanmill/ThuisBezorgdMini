package com.controller;

import com.dto.CustomerOrderDTO;
import com.dto.OrderDTO;
import com.response.ApiResponse;
import com.service.CustomerService;
import com.service.OrderService;
import com.utils.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<List<?>>> getMenuByRestaurant(@PathVariable String slug) {
        List<?> menu = customerService.getMenuByRestaurantSlug(slug);
        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/restaurants/{slug}/orders")
    public ResponseEntity<ApiResponse<OrderDTO>> submitOrder(
            @PathVariable String slug,
            @RequestBody @Valid List<Map<String, Object>> orderItems) {
        OrderDTO order = customerService.submitOrder(slug, orderItems); // ✅ Ensures direct return type
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<CustomerOrderDTO>>> getAllOrdersForUser() {
        List<CustomerOrderDTO> orders = customerService.getAllOrdersForAuthenticatedUser();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PatchMapping("/orders/{orderNumber}/status")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateOrderStatus(
            @PathVariable String orderNumber,
            @RequestBody @Valid Map<String, String> requestBody) {
        ResponseEntity<Map<String, String>> statusUpdateResponse = customerService.updateOrderStatus(orderNumber, requestBody.get("status"));
        Map<String, String> statusUpdate = statusUpdateResponse.getBody(); // ✅ Extract body from ResponseEntity
        return ResponseEntity.ok(ApiResponse.success(statusUpdate));
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/orders/{identifier}")
    public ResponseEntity<ApiResponse<CustomerOrderDTO>> getOrderByIdentifier(@PathVariable String identifier) {
        ResponseEntity<CustomerOrderDTO> orderResponse = customerService.getOrderByIdentifier(AuthUtils.getLoggedInUsername(), identifier);
        CustomerOrderDTO order = orderResponse.getBody(); // ✅ Extract body from ResponseEntity
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
