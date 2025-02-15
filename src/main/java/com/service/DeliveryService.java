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
    public CustomerOrder assignOrder(String identifier) {
        CustomerOrder order = findOrderByIdentifier(identifier);
        order.setDeliveryPerson(getAuthenticatedUsername());
        return customerOrderRepository.save(order);
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
        validateDeliveryPerson(order);

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
     * Validates whether the logged-in delivery person is authorized to manage the given order.
     *
     * @param order The order to validate.
     * @throws RuntimeException if the order is not assigned to the logged-in delivery person.
     */
    protected void validateDeliveryPerson(CustomerOrder order) {
        if (!getAuthenticatedUsername().equals(order.getDeliveryPerson())) {
            throw new RuntimeException("Unauthorized: You are not assigned to this order.");
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
    public Map<String, Object> updateOrderStatus(String identifier, String status) {
        CustomerOrder order = findOrderByIdentifier(identifier);
        validateDeliveryPerson(order);

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

            // ✅ Now using ValidationUtils
            if (!ValidationUtils.isValidStatusTransition(order.getStatus(), newStatus)) {
                throw new RuntimeException("Invalid status transition.");
            }

            order.setStatus(newStatus);
            customerOrderRepository.save(order); // ✅ FIXED: Changed to customerOrderRepository

            return Map.of(
                    "message", "Order status updated successfully.",
                    "orderId", order.getId(),
                    "newStatus", newStatus
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + status);
        }
    }



}
