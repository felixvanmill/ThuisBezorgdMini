package com.service;

import com.dto.CustomerOrderDTO;
import com.exception.ResourceNotFoundException;
import com.exception.ValidationException;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;
import com.utils.AuthUtils;
import com.utils.OrderUtils;
import com.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles delivery-related operations such as assigning orders, confirming pickups, and tracking deliveries.
 */
@Service
public class DeliveryService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    private final CustomerOrderRepository customerOrderRepository;

    /**
     * Constructor-based Dependency Injection for required repositories.
     */
    public DeliveryService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    /**
     * Retrieves all orders relevant to delivery personnel.
     */
    public List<CustomerOrderDTO> getAllDeliveryOrders() {
        List<CustomerOrder> orders = customerOrderRepository.findByStatusesWithDetails(OrderUtils.getActiveDeliveryStatuses());

        if (orders.isEmpty()) {
            logger.info("No active delivery orders found.");
        }

        return orders.stream().map(CustomerOrderDTO::new).collect(Collectors.toList());
    }

    /**
     * Assigns the logged-in delivery person to an order.
     */
    @Transactional
    public Map<String, Object> assignOrder(String identifier) {
        String loggedInUser = AuthUtils.getAuthenticatedUsername();
        if (loggedInUser == null) {
            throw new ValidationException("User must be authenticated to assign an order.");
        }

        CustomerOrder order = OrderUtils.findOrderByIdentifier(customerOrderRepository, identifier);


        order.setDeliveryPerson(loggedInUser);
        customerOrderRepository.save(order);

        logger.info("Order {} assigned to {}", identifier, loggedInUser);

        return Map.of(
                "message", "Delivery person assigned successfully.",
                "orderId", order.getId()
        );
    }

    /**
     * Retrieves assigned orders for the logged-in delivery person.
     */
    public List<CustomerOrderDTO> getAssignedOrders() {
        String loggedInUser = AuthUtils.getAuthenticatedUsername();
        if (loggedInUser == null) {
            throw new ValidationException("User must be authenticated to fetch assigned orders.");
        }

        List<OrderStatus> statuses = OrderUtils.getInProgressStatuses();
        List<CustomerOrder> assignedOrders = customerOrderRepository.findByDeliveryPersonAndStatuses(loggedInUser, statuses);

        if (assignedOrders.isEmpty()) {
            logger.warn("No assigned orders found for user: {}", loggedInUser);
        } else {
            logger.info("Found {} assigned orders for user: {}", assignedOrders.size(), loggedInUser);
        }

        return assignedOrders.stream().map(CustomerOrderDTO::new).collect(Collectors.toList());
    }

    /**
     * Retrieves the delivery history for the logged-in delivery person.
     */
    public List<CustomerOrderDTO> getDeliveryHistory() {
        String loggedInUser = AuthUtils.getAuthenticatedUsername();
        if (loggedInUser == null) {
            throw new ValidationException("User must be authenticated to fetch delivery history.");
        }

        List<CustomerOrder> history = customerOrderRepository.findByDeliveryPersonAndStatuses(loggedInUser, List.of(OrderStatus.DELIVERED));

        if (history.isEmpty()) {
            logger.info("No delivery history found for {}", loggedInUser);
        }

        return history.stream().map(CustomerOrderDTO::new).collect(Collectors.toList());
    }

    /**
     * Retrieves details of a specific order.
     */
    @Transactional(readOnly = true)
    public CustomerOrderDTO getOrderDetails(String identifier) {
        String loggedInUser = AuthUtils.getAuthenticatedUsername();
        if (loggedInUser == null) {
            throw new ValidationException("User must be authenticated to view order details.");
        }

        CustomerOrder order = OrderUtils.findOrderByIdentifier(customerOrderRepository, identifier);


        ValidationUtils.validateDeliveryPerson(order, loggedInUser);
        return new CustomerOrderDTO(order);
    }

    /**
     * Processes order status update.
     */
    @Transactional
    public Map<String, Object> processOrderStatusUpdate(String identifier, Map<String, String> requestBody) {
        String status = ValidationUtils.extractAndValidateStatus(requestBody);
        return updateOrderStatus(identifier, status);
    }

    /**
     * Updates the order status after validation.
     */
    @Transactional
    public Map<String, Object> updateOrderStatus(String identifier, String status) {
        String loggedInUser = AuthUtils.getAuthenticatedUsername();
        if (loggedInUser == null) {
            throw new ValidationException("User must be authenticated to update order status.");
        }

        CustomerOrder order = OrderUtils.findOrderByIdentifier(customerOrderRepository, identifier);


        ValidationUtils.validateDeliveryPerson(order, loggedInUser);
        OrderStatus newStatus = ValidationUtils.parseOrderStatus(status);
        ValidationUtils.ensureValidStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        customerOrderRepository.save(order);

        logger.info("Order {} updated to status {}", identifier, newStatus);

        return Map.of(
                "message", "Order status updated successfully.",
                "orderId", order.getId(),
                "newStatus", newStatus
        );
    }

    /**
     * Retrieves menu items for a specific order.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrderItems(String identifier) {
        String loggedInUser = AuthUtils.getAuthenticatedUsername();
        if (loggedInUser == null) {
            throw new ValidationException("User must be authenticated to view order items.");
        }

        CustomerOrder order = OrderUtils.findOrderByIdentifier(customerOrderRepository, identifier);


        ValidationUtils.validateDeliveryPerson(order, loggedInUser);

        return order.getOrderItems().stream()
                .map(orderItem -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("menuItemName", orderItem.getMenuItem().getName());
                    item.put("quantity", orderItem.getQuantity());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
