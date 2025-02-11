package com.controller;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
import com.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles delivery-related operations such as viewing, managing, and confirming orders.
 */
@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * Constructor-based Dependency Injection for DeliveryService.
     *
     * @param deliveryService The service handling delivery-related operations.
     */
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Retrieves all orders relevant to delivery personnel.
     *
     * @return A response containing a list of available orders for delivery.
     */
    @GetMapping("/allOrders")
    public ResponseEntity<?> getAllOrders() {
        List<CustomerOrderDTO> orders = deliveryService.getAllDeliveryOrders();
        return ResponseEntity.ok(Map.of("orders", orders));
    }

    /**
     * Assigns the logged-in delivery person to an order.
     *
     * @param identifier The order ID or order number.
     * @return A response confirming that the delivery person has been assigned.
     */
    @PostMapping("/orders/{identifier}/assign")
    public ResponseEntity<?> assignDeliveryPerson(@PathVariable String identifier) {
        CustomerOrder order = deliveryService.assignOrder(identifier);
        return ResponseEntity.ok(Map.of(
                "message", "Delivery person assigned successfully.",
                "orderId", order.getId()
        ));
    }

    /**
     * Confirms that the logged-in delivery person has picked up the order.
     *
     * @param identifier The order ID or order number.
     * @return A response confirming that the order is now in PICKING_UP status.
     */
    @PostMapping("/orders/{identifier}/confirmPickup")
    public ResponseEntity<?> confirmPickup(@PathVariable String identifier) {
        CustomerOrder order = deliveryService.confirmPickup(identifier);
        return ResponseEntity.ok(Map.of(
                "message", "Pickup confirmed. Order is now in PICKING_UP status.",
                "orderId", order.getId()
        ));
    }

    /**
     * Confirms that an order has been delivered.
     *
     * @param identifier The order ID or order number.
     * @return A response confirming that the order has been marked as DELIVERED.
     */
    @PostMapping("/orders/{identifier}/confirmDelivery")
    public ResponseEntity<?> confirmDelivery(@PathVariable String identifier) {
        CustomerOrder order = deliveryService.confirmDelivery(identifier);
        return ResponseEntity.ok(Map.of(
                "message", "Delivery confirmed. Order is now DELIVERED.",
                "orderId", order.getId()
        ));
    }

    /**
     * Marks an order as 'In Transport'.
     *
     * @param identifier The order ID or order number.
     * @return A response confirming that the order is now in TRANSPORT status.
     */
    @PostMapping("/orders/{identifier}/confirmTransport")
    public ResponseEntity<?> confirmTransport(@PathVariable String identifier) {
        CustomerOrder order = deliveryService.confirmTransport(identifier);
        return ResponseEntity.ok(Map.of(
                "message", "Order is now in TRANSPORT.",
                "orderId", order.getId()
        ));
    }

    /**
     * Retrieves orders assigned to the logged-in delivery person.
     *
     * @return A response containing a list of assigned orders.
     */
    @GetMapping("/myOrders")
    public ResponseEntity<?> getAssignedOrders() {
        List<CustomerOrderDTO> orders = deliveryService.getAssignedOrders();
        return ResponseEntity.ok(Map.of("orders", orders));
    }

    /**
     * Retrieves the delivery history for the logged-in delivery person.
     *
     * @return A response containing a list of completed deliveries.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getDeliveryHistory() {
        List<CustomerOrderDTO> deliveredOrders = deliveryService.getDeliveryHistory();
        return ResponseEntity.ok(Map.of("deliveredOrders", deliveredOrders));
    }

    /**
     * Retrieves details of a specific order.
     *
     * @param identifier The order ID or order number.
     * @return A response containing detailed order information.
     */
    @GetMapping("/orders/{orderNumber}/details")
    public ResponseEntity<CustomerOrderDTO> getOrderDetails(@PathVariable String orderNumber) {
        CustomerOrderDTO order = deliveryService.getOrderDetails(orderNumber);
        return ResponseEntity.ok(order);
    }

}
