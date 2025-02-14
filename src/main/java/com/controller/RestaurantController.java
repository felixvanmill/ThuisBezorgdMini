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
     * Updates the status of an order for a specific restaurant.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PutMapping("/{slug}/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String slug,
            @PathVariable String orderId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String username = getLoggedInUsername();

            // ✅ Step 1: Ensure employee is assigned to this restaurant
            if (!restaurantService.isEmployeeAuthorizedForRestaurant(username, slug)) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized access"));
            }

            // ✅ Step 2: Fetch restaurant and order
            Restaurant restaurant = restaurantService.getRestaurantBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

            CustomerOrder order = restaurantService.getOrderByOrderNumber(orderId);

            // ✅ Step 3: Validate order ownership
            if (!order.getRestaurant().getId().equals(restaurant.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order does not belong to this restaurant."));
            }

            // ✅ Step 4: Validate and update order status
            String status = requestBody.get("status");
            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status must be provided."));
            }

            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                order.setStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value."));
            }

            // ✅ Step 5: Save and return success
            restaurantService.saveOrder(order);
            return ResponseEntity.ok(Map.of("message", "Order status updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    /**
     * Updates the availability status of a menu item.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PutMapping("/menu/items/availability")
    public ResponseEntity<?> updateMenuItemAvailability(@RequestBody Map<String, Object> request) {
        try {
            // ✅ Validate input
            if (!request.containsKey("menuItemId") || !request.containsKey("isAvailable")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing 'menuItemId' or 'isAvailable' field."));
            }

            Long menuItemId = Long.valueOf(request.get("menuItemId").toString());
            boolean isAvailable = Boolean.parseBoolean(request.get("isAvailable").toString());

            String username = getLoggedInUsername();

            // ✅ Fetch menu item
            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found."));

            // ✅ Ensure the employee is authorized for this restaurant
            if (!restaurantService.isEmployeeAuthorizedForRestaurant(username, menuItem.getRestaurant().getSlug())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized access"));
            }

            // ✅ Update and save
            menuItem.setAvailable(isAvailable);
            menuItemRepository.save(menuItem);

            return ResponseEntity.ok(Map.of(
                    "message", "Menu item availability updated successfully.",
                    "restaurantSlug", menuItem.getRestaurant().getSlug(),
                    "menuItemId", menuItem.getId(),
                    "menuItemName", menuItem.getName(),
                    "newAvailability", menuItem.isAvailable()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Downloads a CSV of orders for the authenticated restaurant.
     */
    @GetMapping(value = "/orders/download", produces = "text/csv")
    public ResponseEntity<?> downloadOrdersAsCsv() {
        try {
            String username = getLoggedInUsername();
            Long restaurantId = getAuthenticatedRestaurantId(username);

            if (restaurantId == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized access"));
            }

            List<OrderDTO> orders = customerOrderRepository.findByRestaurant_IdWithDetails(restaurantId);
            byte[] csvBytes = generateCsvFromDTO(orders).getBytes(StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv")
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .body(csvBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate CSV"));
        }
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
