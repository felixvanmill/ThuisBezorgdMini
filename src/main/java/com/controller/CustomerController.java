package com.controller;

import com.dto.CustomerOrderDTO;
import com.dto.RestaurantDTO;
import com.model.CustomerOrder;
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
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Constructor-based Dependency Injection for CustomerService.
     *
     * @param customerService The service handling customer-related operations.
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Retrieves the menu of a restaurant based on its slug.
     *
     * @param slug The unique restaurant identifier.
     * @return A response containing the restaurant's menu.
     */
    @GetMapping("/{slug}/menu")
    public ResponseEntity<List<?>> getMenuBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(customerService.getMenuByRestaurantSlug(slug));
    }

    /**
     * Places an order for a restaurant.
     *
     * @param slug               The unique identifier for the restaurant.
     * @param menuItemQuantities A map containing menu item IDs as keys and their quantities as values.
     * @return A response with the order details.
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/restaurant/{slug}/order")
    public ResponseEntity<Map<String, Object>> submitOrder(
            @PathVariable String slug,
            @RequestBody Map<Long, Integer> menuItemQuantities) {

        return ResponseEntity.ok(customerService.submitOrder(slug, menuItemQuantities)); // ✅ Correct return type
    }


    /**
     * Tracks the status of an existing order.
     *
     * @param orderId The unique order ID.
     * @return A response containing order status details.
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/track-order/{orderId}")
    public ResponseEntity<CustomerOrderDTO> trackOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(customerService.trackOrder(orderId));
    }

    /**
     * Cancels an order if it is still in an unconfirmed state.
     *
     * @param orderNumber The unique order number.
     * @return A response confirming the order cancellation.
     */
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @PostMapping("/orders/{orderNumber}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable String orderNumber) {
        customerService.cancelOrder(orderNumber);
        return ResponseEntity.ok(Map.of("message", "Order successfully canceled."));
    }

    /**
     * Retrieves a list of all available restaurants.
     *
     * @return ResponseEntity containing a list of Restaurant objects.
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        return ResponseEntity.ok(customerService.getAllRestaurants());
    }
}
