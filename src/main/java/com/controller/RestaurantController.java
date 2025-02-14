package com.controller;

import com.dto.CustomerOrderDTO;
import com.service.CustomerService;
import com.dto.RestaurantDTO;
import com.dto.OrderDTO;
import com.model.CustomerOrder;
import com.model.MenuItem;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.repository.AppUserRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    /**
     * Retrieves a list of all available restaurants with menu items.
     */
    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        boolean isEmployee = isRestaurantEmployee();
        List<RestaurantDTO> restaurantDTOs;

        if (isEmployee) {
            // Get the restaurant ID of the logged-in employee
            String username = getLoggedInUsername();
            Long restaurantId = getAuthenticatedRestaurantId(username);

            if (restaurantId == null) {
                return ResponseEntity.badRequest().body(List.of());
            }

            // Fetch only the assigned restaurant
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            restaurantDTOs = List.of(new RestaurantDTO(
                    restaurant,
                    restaurant.getMenuItems(),
                    true // Include inventory for employees
            ));
        } else {
            // Fetch all restaurants for customers
            restaurantDTOs = restaurantService.getAllRestaurants()
                    .stream()
                    .map(restaurant -> new RestaurantDTO(
                            restaurant,
                            restaurant.getMenuItems(),
                            false // Exclude inventory for customers
                    ))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(restaurantDTOs);
    }



    private boolean isRestaurantEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_RESTAURANT_EMPLOYEE"));
    }


    /**
     * Retrieves the restaurant menu for employees, including inventory details.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/menu-management")
    public ResponseEntity<?> getMenuManagementBySlug(@PathVariable String slug) {
        try {
            RestaurantDTO restaurantDTO = restaurantService.getRestaurantWithMenu(slug, true); // Include inventory
            return ResponseEntity.ok(restaurantDTO.getMenuItems());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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
        try {
            // Extract status from request body
            String status = requestBody.get("status");
            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status must be provided."));
            }

            // Fetch restaurant by slug
            Restaurant restaurant = restaurantService.getRestaurantBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

            // Fetch order by order ID
            CustomerOrder order = restaurantService.getOrderByOrderNumber(orderId);

            // Validate restaurant ownership of the order
            if (!order.getRestaurant().getId().equals(restaurant.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order does not belong to this restaurant."));
            }

            // Validate and update order status safely
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                order.setStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value."));
            }

            // Save updated order
            restaurantService.saveOrder(order);

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated successfully.",
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber(),
                    "newStatus", order.getStatus().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Updates the availability status of a menu item.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PatchMapping("/menu/items/{itemId}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable Long itemId, @RequestBody Map<String, Boolean> request) {
        if (!request.containsKey("isAvailable")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'isAvailable' field."));
        }

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found."));

        menuItem.setAvailable(request.get("isAvailable"));
        menuItemRepository.save(menuItem);

        return ResponseEntity.ok(Map.of(
                "message", "Menu item availability updated successfully.",
                "itemId", menuItem.getId(),
                "isAvailable", menuItem.isAvailable()
        ));
    }

    /**
     * Downloads a CSV of orders for the authenticated restaurant.
     */
    @GetMapping("/orders/download")
    public ResponseEntity<byte[]> downloadOrdersAsCsv() {
        String username = getLoggedInUsername();

        // Fetch orders with eager loading using a custom query
        List<OrderDTO> orders = customerOrderRepository.findByRestaurant_IdWithDetails(
                getAuthenticatedRestaurantId(username)
        );

        // Generate CSV content
        String csvContent = generateCsvFromDTO(orders);
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    /**
     * Generates CSV content from a list of OrderDTOs.
     * This method fetches items for each order using eager loading to avoid lazy initialization issues.
     */
    private String generateCsvFromDTO(List<OrderDTO> orders) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Order Number,Total Price,Status,Customer,Items\n");

        for (OrderDTO order : orders) {
            String items = customerOrderRepository.findByOrderNumberWithItems(order.getOrderNumber())
                    .orElseThrow(() -> new RuntimeException("Order not found"))
                    .getOrderItems().stream()
                    .map(item -> item.getMenuItem().getName() + " x" + item.getQuantity())
                    .collect(Collectors.joining("; "));

            order.setItems(items);

            csvBuilder.append(String.format(
                    "%s,%.2f,%s,%s,%s\n",
                    order.getOrderNumber(),
                    order.getTotalPrice(),
                    order.getStatus(),
                    escapeCsv(order.getCustomer()),
                    escapeCsv(order.getItems())
            ));
        }

        return csvBuilder.toString();
    }

    /**
     * Retrieves the authenticated user's restaurant ID.
     */
    private Long getAuthenticatedRestaurantId(String username) {
        return appUserRepository.findByUsername(username)
                .map(user -> user.getRestaurant() != null ? user.getRestaurant().getId() : null)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found or not associated with a restaurant."));
    }

    /**
     * Escapes CSV-specific characters in a string.
     */
    private String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\n") || input.contains("\"")) {
            input = "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    /**
     * Retrieves the username of the logged-in user.
     */
    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
