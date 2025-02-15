package com.controller;

import com.dto.CustomerOrderDTO;
import com.dto.RestaurantDTO;
import com.model.OrderStatus;
import com.repository.AppUserRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.service.OrderService;
import com.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;
import static com.utils.ResponseUtils.handleRequest;

import java.util.List;
import java.util.Map;

import static com.utils.AuthUtils.getLoggedInUsername;

/**
 * Handles operations related to restaurants, including menu management and order updates.
 */
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private OrderService orderService;


    /**
     * Retrieves a list of all available restaurants with menu items.
     */
    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        boolean isEmployee = com.utils.AuthUtils.isRestaurantEmployee();
        String username = getLoggedInUsername();

        List<RestaurantDTO> restaurantDTOs = isEmployee
                ? restaurantService.getRestaurantsForEmployee(username)
                : restaurantService.getAllRestaurantsWithMenu();

        return ResponseEntity.ok(restaurantDTOs);
    }

    /**
     * Updates the status of an order for a specific restaurant.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PatchMapping("/{slug}/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String slug,
            @PathVariable String orderId,
            @RequestBody Map<String, String> requestBody) {
        return handleRequest(() -> {
            String username = getLoggedInUsername();
            return restaurantService.updateOrderStatus(username, slug, orderId, requestBody);
        });
    }


    /**
     * Updates the availability status of a menu item for a specific restaurant.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PutMapping("/{slug}/menu/items/{menuItemId}")
    public ResponseEntity<?> updateMenuItemAvailability(
            @PathVariable String slug,
            @PathVariable Long menuItemId,
            @RequestBody Map<String, Boolean> request) {
        return handleRequest(() -> {
            String username = getLoggedInUsername();
            return restaurantService.updateMenuItemAvailability(username, slug, menuItemId, request);
        });
    }


    /**
     * Downloads a CSV of orders for the authenticated restaurant.
     */
    @GetMapping("/orders/download")
    public ResponseEntity<byte[]> downloadOrdersAsCsv() {
        String username = getLoggedInUsername();
        return restaurantService.downloadOrdersAsCsv(username);
    }


    /**
     * Fetches orders by ordernumber in format "ORDER001", for restaurant employees
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders/{identifier}")
    public ResponseEntity<?> getOrderByIdentifier(@PathVariable String identifier) {
        return handleRequest(() -> orderService.getOrderByIdentifier(identifier));
    }

    /**
     * Fetches orders by order ID (numeric), for restaurant employees
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders/id/{orderId}")
    public ResponseEntity<CustomerOrderDTO> getOrderById(@PathVariable Long orderId) {
        return handleRequest(() -> new CustomerOrderDTO(orderService.getOrderById(orderId)));
    }


    /**
     * Fetches orders for the logged-in restaurant employee.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/orders")
    public ResponseEntity<?> getOrdersForLoggedInEmployee(@PathVariable String slug) {
        return handleRequest(() -> {
            String username = getLoggedInUsername();
            return restaurantService.getOrdersForEmployee(slug, username);
        });
    }


    /**
     * Fetches orders based on their status.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders")
    public ResponseEntity<List<CustomerOrderDTO>> getOrdersByStatus(
            @RequestParam(required = false) String status) {

        return handleRequest(() -> {
            if (status != null) {
                return restaurantService.getOrdersByStatus(OrderStatus.valueOf(status.toUpperCase()));
            }
            return restaurantService.getAllOrders();
        });
    }

}
