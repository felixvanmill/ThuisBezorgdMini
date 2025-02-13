package com.controller;

import com.dto.CustomerOrderDTO;
import com.dto.MenuItemDTO;
import com.dto.OrderDTO;
import com.dto.RestaurantDTO;
import com.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles customer-related operations, including viewing restaurant menus,
 * placing orders, tracking orders, and canceling orders.
 */
@RestController
@RequestMapping("/api/v1") // Cleaned-up base URL for RESTful API versioning
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Constructor-based Dependency Injection for CustomerService.
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Retrieves a list of all available restaurants.
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        return ResponseEntity.ok(customerService.getAllRestaurants());
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
     * Retrieves an order by its ID.
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<CustomerOrderDTO> trackOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(customerService.trackOrder(orderId));
    }

    /**
     * Cancels an order if it is still in an unconfirmed state.
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/api/v1/orders/{orderNumber}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable String orderNumber) {
        // Updated to return a custom message in the cancellation response
        customerService.cancelOrder(orderNumber);
        return ResponseEntity.ok(Map.of("message", "Order successfully canceled.")); // Added custom cancellation message
    }
}
