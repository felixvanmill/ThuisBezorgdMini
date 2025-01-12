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
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantService restaurantService;

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/management")
    public ResponseEntity<?> getRestaurantManagementBySlug(@PathVariable String slug) {
        try {
            Restaurant restaurant = restaurantService.getRestaurantWithDetailsBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));
            List<CustomerOrder> orders = customerOrderRepository.findByRestaurant_Id(restaurant.getId());
            List<MenuItem> menuItems = restaurant.getMenuItems();

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

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/management")
    public ResponseEntity<?> getManagementForLoggedInEmployee() {
        try {
            String username = getLoggedInUsername();
            Restaurant restaurant = restaurantService.getRestaurantWithDetailsByEmployeeUsername(username)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found for the logged-in employee."));
            return ResponseEntity.ok(Map.of("redirectUrl", "/restaurant/" + restaurant.getSlug() + "/management"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{slug}/menu")
    public ResponseEntity<?> getMenuBySlug(@PathVariable String slug) {
        try {
            RestaurantDTO restaurantDTO = restaurantService.getRestaurantWithMenu(slug);
            return ResponseEntity.ok(restaurantDTO.getMenuItems());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/{slug}/orders/{orderId}/updateStatus")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String slug, @PathVariable Long orderId, @RequestParam String status) {
        try {
            Restaurant restaurant = restaurantService.getRestaurantWithDetailsBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));
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

    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
