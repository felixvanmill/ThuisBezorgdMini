package com.controller;

import com.dto.CustomerOrderDTO;
import com.dto.MenuItemDTO;
import com.dto.OrderDTO;
import com.dto.RestaurantDTO;
import com.model.CustomerOrder;
import com.service.CustomerService;
import com.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     *
     * New: `GET /api/v1/orders`
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

        String newStatus = requestBody.get("status");

        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status must be provided."));
        }

        return ResponseEntity.ok(customerService.updateOrderStatus(orderNumber, newStatus));
    }


    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/orders/{identifier}")
    public ResponseEntity<?> getOrderByIdentifier(@RequestParam(required = false) String username,
                                                  @PathVariable String identifier) {
        try {
            String loggedInUser = getAuthenticatedUsername();

            if (username != null && !loggedInUser.equals(username)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied."));
            }

            CustomerOrder order = identifier.matches("\\d+")
                    ? orderService.getOrderById(Long.parseLong(identifier)) // Numeric order ID
                    : orderService.getOrderForCustomerByOrderNumber(loggedInUser, identifier); // Alphanumeric order number

            return ResponseEntity.ok(new CustomerOrderDTO(order));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


}
