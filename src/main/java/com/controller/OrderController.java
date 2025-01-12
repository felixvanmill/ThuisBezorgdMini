package com.controller;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
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
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantService restaurantService;

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping
    public ResponseEntity<?> getOrdersForLoggedInEmployee(@RequestParam String slug) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<CustomerOrderDTO> orders = restaurantService
                    .getOrdersForEmployee(slug, username)
                    .stream()
                    .map(CustomerOrderDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

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

    @PostMapping
    public ResponseEntity<CustomerOrderDTO> addOrder(@RequestBody final CustomerOrder order) {
        CustomerOrder newOrder = orderService.addOrder(order);
        return ResponseEntity.ok(new CustomerOrderDTO(newOrder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable final Long id, @RequestParam final String status) {
        try {
            CustomerOrder updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(new CustomerOrderDTO(updatedOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable final Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/{slug}/orders/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(
            @PathVariable String slug,
            @PathVariable Long orderId) {
        try {
            restaurantService.confirmOrder(slug, orderId);
            return ResponseEntity.ok(Map.of("message", "Order confirmed successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
