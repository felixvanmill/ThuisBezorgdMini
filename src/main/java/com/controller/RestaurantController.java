package com.controller;

import com.dto.CustomerOrderDTO;
import com.dto.RestaurantDTO;
import com.model.OrderStatus;
import com.response.ApiResponse;
import com.repository.AppUserRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.service.OrderService;
import com.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.utils.AuthUtils.getLoggedInUsername;
import static com.utils.ResponseUtils.handleRequest;

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
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getAllRestaurants() {
        boolean isEmployee = com.utils.AuthUtils.isRestaurantEmployee();
        String username = getLoggedInUsername();

        List<RestaurantDTO> restaurantDTOs = isEmployee
                ? restaurantService.getRestaurantsForEmployee(username)
                : restaurantService.getAllRestaurantsWithMenu();

        return ResponseEntity.ok(ApiResponse.success(restaurantDTOs));
    }

    /**
     * Updates the status of an order for a specific restaurant.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PatchMapping("/{slug}/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateOrderStatus(
            @PathVariable String slug,
            @PathVariable String orderId,
            @RequestBody @Valid Map<String, String> requestBody) {
        return handleRequest(() -> {
            String username = getLoggedInUsername();
            return ApiResponse.success(restaurantService.updateOrderStatus(username, slug, orderId, requestBody));
        });
    }

    /**
     * Updates the availability status of a menu item for a specific restaurant.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PutMapping("/{slug}/menu/items/{menuItemId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMenuItemAvailability(
            @PathVariable String slug,
            @PathVariable Long menuItemId,
            @RequestBody @Valid Map<String, Boolean> request) {
        return handleRequest(() -> {
            String username = getLoggedInUsername();
            return ApiResponse.success(restaurantService.updateMenuItemAvailability(username, slug, menuItemId, request));
        });
    }

    /**
     * Downloads a CSV of orders for the authenticated restaurant.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders/download")
    public ResponseEntity<byte[]> downloadOrdersAsCsv() {
        String username = getLoggedInUsername();
        return restaurantService.downloadOrdersAsCsv(username);
    }

    /**
     * Fetches orders by order number in format "ORDER001", for restaurant employees.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders/{identifier}")
    public ResponseEntity<ApiResponse<CustomerOrderDTO>> getOrderByIdentifier(@PathVariable String identifier) {
        return handleRequest(() -> ApiResponse.success(orderService.getOrderByIdentifier(identifier)));
    }

    /**
     * Fetches orders by order ID (numeric), for restaurant employees.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders/id/{orderId}")
    public ResponseEntity<ApiResponse<CustomerOrderDTO>> getOrderById(@PathVariable Long orderId) {
        return handleRequest(() -> ApiResponse.success(new CustomerOrderDTO(orderService.getOrderById(orderId))));
    }

    /**
     * Fetches orders for the logged-in restaurant employee.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/orders")
    public ResponseEntity<ApiResponse<List<CustomerOrderDTO>>> getOrdersForLoggedInEmployee(@PathVariable String slug) {
        return handleRequest(() -> {
            String username = getLoggedInUsername();
            return ApiResponse.success(restaurantService.getOrdersForEmployee(slug, username));
        });
    }

    /**
     * Fetches orders based on their status.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<CustomerOrderDTO>>> getOrdersByStatus(
            @RequestParam(required = false) String status) {

        return handleRequest(() -> {
            List<CustomerOrderDTO> orders = (status != null)
                    ? restaurantService.getOrdersByStatus(OrderStatus.valueOf(status.toUpperCase()))
                    : restaurantService.getAllOrders();

            return ApiResponse.success(orders);
        });
    }
}
