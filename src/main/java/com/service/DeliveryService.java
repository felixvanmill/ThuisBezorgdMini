package com.service;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;
import com.utils.ValidationUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * Handles delivery-related operations such as assigning orders, confirming pickups, and tracking deliveries.
 */
@Service
public class DeliveryService {

    private final CustomerOrderRepository customerOrderRepository;

    /**
     * Constructor-based Dependency Injection for required repositories.
     *
     * @param customerOrderRepository The repository handling customer orders.
     */
    public DeliveryService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    /**
     * Retrieves all orders that are relevant to delivery personnel.
     *
     * @return A list of orders that are READY_FOR_DELIVERY, PICKING_UP, or TRANSPORT.
     */
    public List<CustomerOrderDTO> getAllDeliveryOrders() {
        List<OrderStatus> statuses = List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.PICKING_UP, OrderStatus.TRANSPORT);
        return customerOrderRepository.findByStatusesWithDetails(statuses).stream()
                .map(CustomerOrderDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Assigns the logged-in delivery person to a specific order.
     *
     * @param identifier The order ID or order number.
     * @return The updated order with the assigned delivery person.
     * @throws RuntimeException if the order is not found.
     */
    @Transactional
    public Map<String, Object> assignOrder(String identifier) {
        CustomerOrder order = findOrderByIdentifier(identifier);
        order.setDeliveryPerson(getAuthenticatedUsername());
        customerOrderRepository.save(order);

        return Map.of(
                "message", "Delivery person assigned successfully.",
                "orderId", order.getId()
        );
    }


    /**
     * Retrieves the orders assigned to the currently logged-in delivery person.
     *
     * @return A list of assigned orders in PICKING_UP or TRANSPORT status.
     */
    public List<CustomerOrderDTO> getAssignedOrders() {
        return customerOrderRepository.findByDeliveryPersonAndStatuses(
                        getAuthenticatedUsername(),
                        List.of(OrderStatus.PICKING_UP, OrderStatus.TRANSPORT))
                .stream()
                .map(CustomerOrderDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the delivery history of the currently logged-in delivery person.
     *
     * @return A list of completed (DELIVERED) orders.
     */
    public List<CustomerOrderDTO> getDeliveryHistory() {
        return customerOrderRepository.findByDeliveryPersonAndStatuses(
                        getAuthenticatedUsername(),
                        List.of(OrderStatus.DELIVERED))
                .stream()
                .map(CustomerOrderDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves detailed information about a specific order.
     *
     * @param identifier The order ID or order number.
     * @return The order details.
     * @throws RuntimeException if the order is not assigned to the logged-in delivery person.
     */
    @Transactional(readOnly = true)
    public CustomerOrderDTO getOrderDetails(String identifier) {
        CustomerOrder order = findOrderByIdentifier(identifier);
        ValidationUtils.validateDeliveryPerson(order, getAuthenticatedUsername()); // ✅ Fixed
        return new CustomerOrderDTO(order);
    }



    /**
     * Finds an order by its ID or order number.
     *
     * @param identifier The order ID (numeric) or order number (alphanumeric).
     * @return The corresponding order.
     * @throws RuntimeException if the order is not found.
     */
    private CustomerOrder findOrderByIdentifier(String identifier) {
        if (identifier.matches("\\d+")) { // Numeric order ID
            Long orderId = Long.parseLong(identifier);
            return customerOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        } else { // Alphanumeric order number
            return customerOrderRepository.findByOrderNumber(identifier)
                    .orElseThrow(() -> new RuntimeException("Order not found with order number: " + identifier));
        }
    }


    /**
     * Retrieves the username of the currently authenticated delivery person.
     *
     * @return The username of the logged-in delivery person.
     */
    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    @Transactional
    public Map<String, Object> processOrderStatusUpdate(String identifier, Map<String, String> requestBody) {
        String status = extractAndValidateStatus(requestBody); // Extract status safely
        return updateOrderStatus(identifier, status);
    }

    /**
     * Extracts and validates the status field from the request body.
     *
     * @param requestBody The request payload containing the status field.
     * @return The validated status value.
     * @throws IllegalArgumentException if the status is missing or invalid.
     */
    private String extractAndValidateStatus(Map<String, String> requestBody) {
        if (!ValidationUtils.isValidStatus(requestBody, "status")) {
            throw new IllegalArgumentException("Status must be provided and cannot be empty.");
        }
        return requestBody.get("status");
    }

    /**
     * Updates the order status after validation.
     */
    @Transactional
    public Map<String, Object> updateOrderStatus(String identifier, String status) {
        CustomerOrder order = findOrderByIdentifier(identifier);
        String loggedInUser = getAuthenticatedUsername();

        ValidationUtils.validateDeliveryPerson(order, loggedInUser);

        OrderStatus newStatus = parseOrderStatus(status);

        if (!ValidationUtils.isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        customerOrderRepository.save(order);

        return Map.of(
                "message", "Order status updated successfully.",
                "orderId", order.getId(),
                "newStatus", newStatus
        );
    }

    /**
     * Parses and validates the order status.
     */
    private OrderStatus parseOrderStatus(String status) {
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status provided: " + status);
        }
    }

    /**
     * Retrieves menu items for a specific order along with their quantities.
     *
     * @param identifier The order ID or order number.
     * @return A list of menu items with their quantities.
     * @throws RuntimeException if the order is not found or unauthorized.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrderItems(String identifier) {
        CustomerOrder order = customerOrderRepository.findByOrderNumberWithItems(identifier)
                .orElseThrow(() -> new RuntimeException("Order not found with identifier: " + identifier));

        ValidationUtils.validateDeliveryPerson(order, getAuthenticatedUsername());

        return order.getOrderItems().stream()
                .map(orderItem -> Map.of(
                        "menuItemName", (Object) orderItem.getMenuItem().getName(),
                        "quantity", (Object) orderItem.getQuantity() // ✅ Explicit cast to Object
                ))
                .collect(Collectors.toList());
    }

}
