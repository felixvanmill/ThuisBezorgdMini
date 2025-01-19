package com.controller;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles delivery-related operations such as viewing, managing, and confirming orders.
 */
@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    /**
     * Fetches all orders relevant to delivery personnel (e.g., READY_FOR_DELIVERY, PICKING_UP, TRANSPORT).
     */
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

        return ResponseEntity.ok(Map.of(
                "username", getLoggedInUsername(),
                "orders", orders
        ));
    }

    /**
     * Assigns the logged-in delivery person to an order by ID or order number.
     */
    @PostMapping("/orders/{identifier}/assign")
    public ResponseEntity<?> assignDeliveryPerson(@PathVariable String identifier) {
        try {
            CustomerOrder order = findOrderByIdentifier(identifier);

            // Assign the logged-in user to the order
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

    /**
     * Confirms that the logged-in delivery person has picked up the order.
     */
    @PostMapping("/orders/{identifier}/confirmPickup")
    public ResponseEntity<?> confirmPickup(@PathVariable String identifier) {
        try {
            CustomerOrder order = findOrderByIdentifier(identifier);
            String username = getLoggedInUsername();

            if (!isAuthorizedDeliveryPerson(order, username)) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
            }

            if (OrderStatus.READY_FOR_DELIVERY != order.getStatus()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order is not in READY_FOR_DELIVERY status."));
            }

            // Update order status
            order.setStatus(OrderStatus.PICKING_UP);
            customerOrderRepository.save(order);

            return ResponseEntity.ok(Map.of(
                    "message", "Pickup confirmed. Order is now in PICKING_UP status.",
                    "orderId", order.getId(),
                    "orderNumber", order.getOrderNumber()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Confirms delivery of an order by the logged-in delivery person.
     */
    @PostMapping("/orders/{identifier}/confirmDelivery")
    public ResponseEntity<?> confirmDelivery(@PathVariable String identifier) {
        try {
            CustomerOrder order = findOrderByIdentifier(identifier);
            String username = getLoggedInUsername();

            if (!username.equals(order.getDeliveryPerson())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
            }

            if (OrderStatus.TRANSPORT != order.getStatus()) {
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

    /**
     * Fetches orders assigned to the logged-in delivery person.
     */
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

    /**
     * Fetches delivery history for the logged-in delivery person.
     */
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

    /**
     * Fetches detailed information for a specific order.
     */
    @GetMapping("/orders/{identifier}/details")
    public ResponseEntity<?> getOrderDetails(@PathVariable String identifier) {
        try {
            CustomerOrder order = findOrderDetailsByIdentifier(identifier);
            String username = getLoggedInUsername();

            if (!username.equals(order.getDeliveryPerson())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
            }

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

    @PostMapping("/orders/{identifier}/confirmTransport")
    public ResponseEntity<?> confirmTransport(@PathVariable String identifier) {
        try {
            CustomerOrder order = findOrderByIdentifier(identifier);
            String username = getLoggedInUsername();

            if (!username.equals(order.getDeliveryPerson())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized: You are not assigned to this order."));
            }

            if (OrderStatus.PICKING_UP != order.getStatus()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order is not in PICKING_UP status."));
            }

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

    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private CustomerOrder findOrderByIdentifier(String identifier) {
        if (identifier.matches("\\d+")) {
            Long orderId = Long.parseLong(identifier);
            return customerOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        } else {
            return customerOrderRepository.findByOrderNumber(identifier)
                    .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
        }
    }

    private CustomerOrder findOrderDetailsByIdentifier(String identifier) {
        if (identifier.matches("\\d+")) {
            Long orderId = Long.parseLong(identifier);
            return customerOrderRepository.findByIdWithDetails(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        } else {
            return customerOrderRepository.findByOrderNumberWithDetails(identifier)
                    .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
        }
    }

    private boolean isAuthorizedDeliveryPerson(CustomerOrder order, String username) {
        if (order.getDeliveryPerson() == null) {
            order.setDeliveryPerson(username);
            customerOrderRepository.save(order);
            return true;
        }
        return username.equals(order.getDeliveryPerson());
    }
}
