package com.controller;

import com.dto.RestaurantDTO;
import com.model.CustomerOrder;
import com.model.MenuItem;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.service.RestaurantService;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.RestaurantRepository;
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
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantService restaurantService;

    /**
     * Fetch the management dashboard details for a restaurant based on its slug.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/management")
    public ResponseEntity<?> getRestaurantManagementBySlug(@PathVariable String slug) {
        try {
            Restaurant restaurant = validateRestaurant(slug);
            List<CustomerOrder> orders = customerOrderRepository.findByRestaurant_Id(restaurant.getId());
            List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());

            return ResponseEntity.ok(Map.of(
                    "restaurantName", restaurant.getName(),
                    "orders", orders,
                    "menuItems", menuItems,
                    "slug", slug
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Redirects the employee to the management dashboard of their assigned restaurant.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/management")
    public ResponseEntity<?> getManagementForLoggedInEmployee() {
        try {
            String username = getLoggedInUsername();
            Restaurant restaurant = restaurantRepository.findByEmployees_Username(username)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found for the logged-in employee."));
            return ResponseEntity.ok(Map.of("redirectUrl", "/restaurant/" + restaurant.getSlug() + "/management"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Fetch the menu of a restaurant by its slug.
     */
    @GetMapping("/{slug}/menu")
    public ResponseEntity<?> getMenuBySlug(@PathVariable String slug) {
        try {
            RestaurantDTO restaurantDTO = restaurantService.getRestaurantWithMenu(slug);
            return ResponseEntity.ok(restaurantDTO.getMenuItems());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Fetch the menu for the restaurant where the logged-in employee works.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/menu")
    public ResponseEntity<?> getMenuForLoggedInEmployee() {
        try {
            String username = getLoggedInUsername();
            Restaurant restaurant = restaurantRepository.findByEmployees_Username(username)
                    .orElseThrow(() -> new RuntimeException("No restaurant found for the logged-in employee."));
            List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());
            return ResponseEntity.ok(menuItems);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update the status of a specific customer order.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/{slug}/orders/{orderId}/updateStatus")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String slug, @PathVariable Long orderId, @RequestParam String status) {
        try {
            Restaurant restaurant = validateRestaurant(slug);
            CustomerOrder order = customerOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            if (!order.getRestaurant().getId().equals(restaurant.getId())) {
                throw new RuntimeException("Order does not belong to the specified restaurant.");
            }
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            order.setStatus(orderStatus);
            customerOrderRepository.save(order);
            return ResponseEntity.ok(Map.of("message", "Order status updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Validates the restaurant based on the provided slug.
     */
    private Restaurant validateRestaurant(String slug) {
        return restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));
    }

    /**
     * Helper method to get the username of the logged-in user.
     */
    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
