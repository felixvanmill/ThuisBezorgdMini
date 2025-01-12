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

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    // Fetch all orders with statuses relevant to delivery personnel
    @GetMapping("/allOrders")
    public ResponseEntity<?> getAllOrders() {
        List<OrderStatus> statuses = List.of(
                OrderStatus.READY_FOR_DELIVERY,
                OrderStatus.PICKING_UP,
                OrderStatus.TRANSPORT
        );
        List<CustomerOrderDTO> orders = customerOrderRepository.findByStatusesWithDetails(statuses).stream()
                .map(CustomerOrderDTO::new)
                .collect(Collectors.toList());
        String username = getLoggedInUsername();

        return ResponseEntity.ok(Map.of(
                "username", username,
                "orders", orders
        ));
    }

    // Assign a delivery person to an order using either order ID or order number
    @PostMapping("/orders/{identifier}/assign")
    public ResponseEntity<?> assignDeliveryPerson(@PathVariable String identifier) {
        CustomerOrder order;

        try {
            if (identifier.matches("\\d+")) { // If identifier is numeric, treat it as order ID
                Long orderId = Long.parseLong(identifier);
                order = customerOrderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            } else { // Otherwise, treat it as an order number
                order = customerOrderRepository.findByOrderNumber(identifier)
                        .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
            }

            String username = getLoggedInUsername();
            order.setDeliveryPerson(username);
            customerOrderRepository.save(order);

            return ResponseEntity.ok(Map.of(
                    "message", "Delivery person assigned successfully.",
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Confirm pickup of an order using either numeric ID or alphanumeric order number
    @PostMapping("/orders/{identifier}/confirmPickup")
    public ResponseEntity<?> confirmPickup(@PathVariable String identifier) {
        CustomerOrder order;

        try {
            // Identify whether the identifier is numeric or alphanumeric
            if (identifier.matches("\\d+")) {
                Long orderId = Long.parseLong(identifier);
                order = customerOrderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            } else {
                order = customerOrderRepository.findByOrderNumber(identifier)
                        .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
            }

            String username = getLoggedInUsername();

            // Validate that the logged-in user is assigned to the order
            if (order.getDeliveryPerson() == null) {
                // If the order is unassigned, assign it to the logged-in delivery person
                order.setDeliveryPerson(username);
            } else if (!username.equals(order.getDeliveryPerson())) {
                // If already assigned to someone else, reject the request
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
            }

            // Ensure the order is in the correct status for pickup confirmation
            if (order.getStatus() != OrderStatus.READY_FOR_DELIVERY) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order is not in READY_FOR_DELIVERY status."));
            }

            // Update the status to PICKING_UP
            order.setStatus(OrderStatus.PICKING_UP);
            customerOrderRepository.save(order);

            return ResponseEntity.ok(Map.of(
                    "message", "Pickup confirmed. Order is now in PICKING_UP.",
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    // Confirm delivery of an order
    @PostMapping("/orders/{identifier}/confirmDelivery")
    public ResponseEntity<?> confirmDelivery(@PathVariable String identifier) {
        CustomerOrder order;

        try {
            if (identifier.matches("\\d+")) {
                Long orderId = Long.parseLong(identifier);
                order = customerOrderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            } else {
                order = customerOrderRepository.findByOrderNumber(identifier)
                        .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
            }

            String username = getLoggedInUsername();

            if (!username.equals(order.getDeliveryPerson())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
            }

            if (order.getStatus() != OrderStatus.TRANSPORT) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order is not in TRANSPORT status."));
            }

            order.setStatus(OrderStatus.DELIVERED);
            customerOrderRepository.save(order);

            return ResponseEntity.ok(Map.of(
                    "message", "Delivery confirmed. Order is now marked as DELIVERED.",
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Fetch orders assigned to the logged-in delivery person
    @GetMapping("/myOrders")
    public ResponseEntity<?> getAssignedOrders() {
        String username = getLoggedInUsername();
        List<CustomerOrderDTO> orders = customerOrderRepository.findByDeliveryPersonAndStatuses(
                        username,
                        List.of(OrderStatus.PICKING_UP, OrderStatus.TRANSPORT)
                ).stream()
                .map(CustomerOrderDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "username", username,
                "orders", orders
        ));
    }

    // Retrieve delivery history for the logged-in delivery person
    @GetMapping("/history")
    public ResponseEntity<?> getDeliveryHistory() {
        String username = getLoggedInUsername();
        List<CustomerOrderDTO> deliveredOrders = customerOrderRepository.findByDeliveryPersonAndStatuses(
                        username,
                        List.of(OrderStatus.DELIVERED)
                ).stream()
                .map(CustomerOrderDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "username", username,
                "deliveredOrders", deliveredOrders
        ));
    }

    // Utility method: Get the logged-in user's username
    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PostMapping("/orders/{identifier}/confirmTransport")
    public ResponseEntity<?> confirmTransport(@PathVariable String identifier) {
        CustomerOrder order;

        try {
            // Identify whether the identifier is numeric or alphanumeric
            if (identifier.matches("\\d+")) {
                Long orderId = Long.parseLong(identifier);
                order = customerOrderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            } else {
                order = customerOrderRepository.findByOrderNumber(identifier)
                        .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
            }

            String username = getLoggedInUsername();

            // Validate that the logged-in user is assigned to the order
            if (!username.equals(order.getDeliveryPerson())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
            }

            // Ensure the order is in the correct status for transport confirmation
            if (order.getStatus() != OrderStatus.PICKING_UP) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order is not in PICKING_UP status."));
            }

            // Update the status to TRANSPORT
            order.setStatus(OrderStatus.TRANSPORT);
            customerOrderRepository.save(order);

            return ResponseEntity.ok(Map.of(
                    "message", "Order is now in TRANSPORT.",
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Fetch detailed information for a specific order
    @GetMapping("/orders/{identifier}/details")
    public ResponseEntity<?> getOrderDetails(@PathVariable String identifier) {
        CustomerOrder order;

        try {
            if (identifier.matches("\\d+")) {
                Long orderId = Long.parseLong(identifier);
                order = customerOrderRepository.findByIdWithDetails(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
            } else {
                order = customerOrderRepository.findByOrderNumberWithDetails(identifier)
                        .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
            }

            String username = getLoggedInUsername();

            // Ensure the logged-in user is authorized to access the order
            if (!username.equals(order.getDeliveryPerson())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
            }

            // Map the order details to a response
            return ResponseEntity.ok(Map.of(
                    "orderNumber", order.getOrderNumber(),
                    "deliveryAddress", order.getAddress().getFullAddress(),
                    "items", order.getOrderItems().stream()
                            .map(item -> Map.of(
                                    "itemName", item.getMenuItem().getName(),
                                    "quantity", item.getQuantity(),
                                    "price", item.getMenuItem().getPrice(),
                                    "totalPrice", item.getTotalPrice()
                            ))
                            .collect(Collectors.toList()),
                    "totalPrice", order.getTotalPrice(),
                    "status", order.getStatus().name(),
                    "restaurant", order.getRestaurant().getName()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


}
