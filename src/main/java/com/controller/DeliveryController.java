package com.controller;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController // Ensures all methods return JSON responses by default
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    // ✅ Fetch all orders with relevant statuses
    @GetMapping("/allOrders")
    public ResponseEntity<?> getAllOrders() {
        List<OrderStatus> statuses = List.of(
                OrderStatus.READY_FOR_DELIVERY,
                OrderStatus.PICKING_UP,
                OrderStatus.TRANSPORT
        );
        List<CustomerOrderDTO> orders = customerOrderRepository.findByStatusesWithDetails(statuses).stream()
                .map(CustomerOrderDTO::new) // Map each entity to DTO
                .collect(Collectors.toList());
        String username = getLoggedInUsername();

        return ResponseEntity.ok(Map.of(
                "username", username,
                "orders", orders
        ));
    }

    // ✅ Assign a delivery person to an order
    @PostMapping("/orders/{orderId}/assign")
    public ResponseEntity<?> assignDeliveryPerson(@PathVariable Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        String username = getLoggedInUsername();
        order.setDeliveryPerson(username);
        customerOrderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "message", "Delivery person assigned successfully.",
                "orderId", orderId
        ));
    }

    // ✅ Fetch orders assigned to the logged-in delivery person
    @GetMapping("/myOrders")
    public ResponseEntity<?> getAssignedOrders() {
        String username = getLoggedInUsername();
        List<CustomerOrderDTO> orders = customerOrderRepository.findByDeliveryPersonAndStatuses(
                        username,
                        List.of(OrderStatus.PICKING_UP, OrderStatus.TRANSPORT)
                ).stream()
                .map(CustomerOrderDTO::new) // Map to DTO
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "username", username,
                "orders", orders
        ));
    }

    // ✅ Update the status of an order
    @PostMapping("/orders/{orderId}/updateStatus")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        String username = getLoggedInUsername();
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!username.equals(order.getDeliveryPerson())) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
            customerOrderRepository.save(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value: " + status));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Order status updated successfully.",
                "orderId", orderId
        ));
    }

    // ✅ Fetch available unassigned orders
    @GetMapping("/availableOrders")
    public ResponseEntity<?> getAvailableOrders() {
        List<CustomerOrderDTO> orders = customerOrderRepository.findUnassignedOrdersByStatus(OrderStatus.READY_FOR_DELIVERY).stream()
                .map(CustomerOrderDTO::new) // Map to DTO
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("orders", orders));
    }

    // ✅ Confirm and assign an unassigned order to the logged-in delivery person
    @PostMapping("/orders/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (order.getDeliveryPerson() != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Order already assigned to another delivery person."));
        }

        String username = getLoggedInUsername();
        order.setDeliveryPerson(username);
        order.setStatus(OrderStatus.PICKING_UP);
        customerOrderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "message", "Order confirmed and assigned successfully.",
                "orderId", orderId
        ));
    }

    // ✅ Utility method: Get the logged-in user's username
    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
