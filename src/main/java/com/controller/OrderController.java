package com.controller;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.service.OrderService;
import com.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantService restaurantService;

    // Customer-specific endpoints
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public ResponseEntity<?> getOrdersForCustomer(@RequestParam String username) {
        try {
            String loggedInUser = getAuthenticatedUsername();
            if (!loggedInUser.equals(username)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied."));
            }
            List<CustomerOrderDTO> orders = orderService.getOrdersForCustomer(username)
                    .stream()
                    .map(CustomerOrderDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer/{orderNumber}")
    public ResponseEntity<?> getCustomerOrderByOrderNumber(@RequestParam String username, @PathVariable String orderNumber) {
        try {
            String loggedInUser = getAuthenticatedUsername();
            if (!loggedInUser.equals(username)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied."));
            }
            CustomerOrder order = orderService.getOrderForCustomerByOrderNumber(username, orderNumber);
            return ResponseEntity.ok(new CustomerOrderDTO(order));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Restaurant-specific endpoints
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/restaurant")
    public ResponseEntity<?> getOrdersForLoggedInEmployee(@RequestParam String slug) {
        try {
            String username = getAuthenticatedUsername();
            List<CustomerOrderDTO> orders = restaurantService.getOrdersForEmployee(slug, username)
                    .stream()
                    .map(CustomerOrderDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/restaurant/{slug}/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable String slug, @PathVariable Long orderId) {
        try {
            restaurantService.confirmOrder(slug, orderId);
            return ResponseEntity.ok(Map.of("message", "Order confirmed successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Shared endpoints
    @GetMapping("/{identifier}")
    public ResponseEntity<?> getOrderByIdentifier(@PathVariable String identifier) {
        try {
            CustomerOrder order;
            if (identifier.matches("\\d+")) { // Numeric ID
                order = orderService.getOrderById(Long.parseLong(identifier));
            } else { // Alphanumeric order number
                order = orderService.getOrderByOrderNumber(identifier);
            }
            return ResponseEntity.ok(new CustomerOrderDTO(order));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/restaurant/{slug}/{orderId}/updateStatus")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String slug,
            @PathVariable String orderId, // Keep as String for alphanumeric IDs
            @RequestParam String status) {
        try {
            // Determine if `orderId` is numeric or alphanumeric
            CustomerOrder order;
            if (orderId.matches("\\d+")) {
                order = orderService.getOrderById(Long.parseLong(orderId));
            } else {
                order = orderService.getOrderByOrderNumber(orderId);
            }

            if (!order.getRestaurant().getSlug().equals(slug)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order does not belong to this restaurant."));
            }

            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
            orderService.saveOrder(order);

            return ResponseEntity.ok(Map.of("message", "Order status updated successfully.", "newStatus", order.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value: " + status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Add this endpoint to fetch orders by status
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/status")
    public ResponseEntity<?> getOrdersByStatus(@RequestParam String status) {
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
