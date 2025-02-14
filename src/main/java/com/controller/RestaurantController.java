package com.controller;

import com.dto.CustomerOrderDTO;
import com.dto.RestaurantDTO;
import com.dto.OrderDTO;
import com.model.CustomerOrder;
import com.model.MenuItem;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.repository.AppUserRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.service.OrderService;
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

    @Autowired
    private OrderService orderService;


    /**
     * Retrieves a list of all available restaurants with menu items.
     */
    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        boolean isEmployee = com.utils.AuthUtils.isRestaurantEmployee();
        String username = com.utils.AuthUtils.getLoggedInUsername();

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
        try {
            String username = com.utils.AuthUtils.getLoggedInUsername();
            return restaurantService.updateOrderStatus(username, slug, orderId, requestBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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
        try {
            // ✅ Validate request body
            if (!request.containsKey("isAvailable")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing 'isAvailable' field."));
            }

            boolean isAvailable = request.get("isAvailable");

            String username = getLoggedInUsername();

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found."));

            if (!restaurantService.isEmployeeAuthorizedForRestaurant(username, slug)) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized access"));
            }

            menuItem.setAvailable(isAvailable);
            menuItemRepository.save(menuItem);

            return ResponseEntity.ok(Map.of(
                    "message", "Menu item availability updated successfully.",
                    "restaurantSlug", slug,
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

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders/{identifier}")
    public ResponseEntity<?> getOrderByIdentifier(@PathVariable String identifier) {
        try {
            CustomerOrder order = identifier.matches("\\d+")
                    ? customerOrderRepository.findById(Long.parseLong(identifier))
                    .orElseThrow(() -> new RuntimeException("Order not found"))
                    : customerOrderRepository.findByOrderNumber(identifier)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            return ResponseEntity.ok(new CustomerOrderDTO(order));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Fetches orders for the logged-in restaurant employee.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/orders")
    public ResponseEntity<?> getOrdersForLoggedInEmployee(@PathVariable String slug) {
        try {
            String username = getLoggedInUsername();
            List<CustomerOrderDTO> orders = restaurantService.getOrdersForEmployee(slug, username).stream()
                    .map(CustomerOrderDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Fetches orders based on their status.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<CustomerOrderDTO> orders = orderService.getOrdersByStatus(orderStatus).stream()
                    .map(CustomerOrderDTO::new)
                    .collect(Collectors.toList());

            if (orders.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "No orders found with status: " + status));
            }

            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value: " + status));
        }
    }

}
