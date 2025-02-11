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

/**
 * Handles operations related to customer and restaurant orders, including viewing, updating, and managing order statuses.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantService restaurantService;

    /**
     * Fetches orders for a specific customer.
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public ResponseEntity<?> getOrdersForCustomer(@RequestParam String username) {
        try {
            String loggedInUser = getAuthenticatedUsername();
            if (!loggedInUser.equals(username)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied."));
            }
            List<CustomerOrderDTO> orders = orderService.getOrdersForCustomer(username).stream()
                    .map(CustomerOrderDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Fetches a specific customer order by order number.
     */
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

    /**
     * Fetches orders for the logged-in restaurant employee.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/restaurant")
    public ResponseEntity<?> getOrdersForLoggedInEmployee(@RequestParam String slug) {
        try {
            String username = getAuthenticatedUsername();
            List<CustomerOrderDTO> orders = restaurantService.getOrdersForEmployee(slug, username).stream()
                    .map(CustomerOrderDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Confirms an order for a specific restaurant.
     */
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

    /**
     * Fetches an order by its numeric ID or alphanumeric order number.
     */
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{identifier}")
    public ResponseEntity<?> getOrderByIdentifier(@PathVariable String identifier) {
        try {
            CustomerOrder order = identifier.matches("\\d+")
                    ? orderService.getOrderById(Long.parseLong(identifier))
                    : orderService.getOrderByOrderNumber(identifier);

            return ResponseEntity.ok(new CustomerOrderDTO(order));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Updates the status of an order for a specific restaurant.
     */
    @PostMapping("/restaurant/{slug}/{orderId}/updateStatus")
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String slug,
            @PathVariable String orderId, // Can handle alphanumeric IDs
            @RequestParam String status) {
        try {
            CustomerOrder order = orderId.matches("\\d+")
                    ? orderService.getOrderById(Long.parseLong(orderId))
                    : orderService.getOrderByOrderNumber(orderId); // 🚨 Wrong return type here



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


    /**
     * Fetches orders based on their status.
     */
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

    /**
     * Retrieves the username of the currently authenticated user.
     */
    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
