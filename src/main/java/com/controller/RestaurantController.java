package com.controller;

import com.dto.RestaurantDTO;
import com.model.CustomerOrder;
import com.model.MenuItem;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.service.RestaurantService;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    /**
     * For customers: Fetch the restaurant menu without inventory details.
     */
    @GetMapping("/{slug}/menu")
    public ResponseEntity<?> getMenuBySlug(@PathVariable String slug) {
        try {
            RestaurantDTO restaurantDTO = restaurantService.getRestaurantWithMenu(slug, false); // Exclude inventory
            return ResponseEntity.ok(restaurantDTO.getMenuItems());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * For restaurant employees: Fetch the restaurant menu with inventory details.
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
     * For restaurant employees: Update the status of an order.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/{slug}/orders/{identifier}/updateStatus")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String slug,
            @PathVariable String identifier,
            @RequestParam String status) {
        try {
            Restaurant restaurant = restaurantService.getRestaurantBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

            CustomerOrder order;
            if (identifier.matches("\\d+")) {
                Long orderId = Long.parseLong(identifier);
                order = restaurantService.getOrderById(orderId);
            } else {
                order = restaurantService.getOrderByOrderNumber(identifier);
            }

            if (!order.getRestaurant().getId().equals(restaurant.getId())) {
                throw new RuntimeException("Order does not belong to the specified restaurant.");
            }

            // Allow only valid status transitions
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            if (orderStatus == OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.UNCONFIRMED) {
                throw new RuntimeException("Order must be UNCONFIRMED to transition to CONFIRMED.");
            }

            order.setStatus(orderStatus);
            restaurantService.saveOrder(order);

            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated successfully.",
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber(),
                    "newStatus", status
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * For restaurant employees: Update the availability status of a menu item.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PatchMapping("/menu/items/{itemId}/availability")
    public ResponseEntity<?> updateAvailability(@PathVariable Long itemId, @RequestBody Map<String, Boolean> request) {
        if (!request.containsKey("isAvailable")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'isAvailable' field"));
        }

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        menuItem.setAvailable(request.get("isAvailable"));
        menuItemRepository.save(menuItem);

        return ResponseEntity.ok(Map.of(
                "message", "Menu item availability updated successfully",
                "itemId", menuItem.getId(),
                "isAvailable", menuItem.isAvailable()
        ));
    }

    /**
     * Helper method to get the username of the logged-in user.
     */
    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
