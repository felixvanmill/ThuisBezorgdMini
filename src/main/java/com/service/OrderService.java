// src/main/java/com/service/OrderService.java

package com.service;

import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CustomerOrderRepository orderRepository;

    /**
     * Get all orders.
     */
    @Transactional(readOnly = true)
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Get an order by ID.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
    }

    /**
     * Get an order by order number.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    /**
     * Add a new order.
     */
    public CustomerOrder addOrder(CustomerOrder order) {
        return orderRepository.save(order);
    }

    /**
     * Update the status of an order.
     */
    public CustomerOrder updateOrderStatus(Long id, String status) {
        CustomerOrder order = getOrderById(id);
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
            return orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid OrderStatus: " + status);
        }
    }

    /**
     * Delete an order by ID.
     */
    public void deleteOrder(Long id) {
        CustomerOrder order = getOrderById(id);
        orderRepository.delete(order);
    }

    /**
     * Get orders by status.
     */
    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get orders for a specific customer by username.
     */
    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersForCustomer(String username) {
        return orderRepository.findByUser_Username(username);
    }

    /**
     * Get a specific order for a customer by order number.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderForCustomerByOrderNumber(String username, String orderNumber) {
        return orderRepository.findByOrderNumberAndUser_Username(orderNumber, username)
                .orElseThrow(() -> new RuntimeException("Order not found for user " + username + " with order number: " + orderNumber));
    }

    /**
     * Save or update an order.
     */
    public CustomerOrder saveOrder(CustomerOrder order) {
        return orderRepository.save(order);
    }
}
