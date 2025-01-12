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

    @Transactional(readOnly = true)
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(final Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    if (order.getRestaurant() != null) {
                        order.getRestaurant().getName();
                    }
                    if (order.getOrderItems() != null) {
                        order.getOrderItems().size();
                    }
                    return order;
                })
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderByOrderNumber(final String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(order -> {
                    if (order.getRestaurant() != null) {
                        order.getRestaurant().getName();
                    }
                    if (order.getOrderItems() != null) {
                        order.getOrderItems().size();
                    }
                    return order;
                })
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    public CustomerOrder addOrder(final CustomerOrder order) {
        return orderRepository.save(order);
    }

    public CustomerOrder updateOrderStatus(final Long id, final String status) {
        CustomerOrder order = getOrderById(id);
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
            return orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid OrderStatus: " + status);
        }
    }

    public void deleteOrder(final Long id) {
        CustomerOrder order = getOrderById(id);
        orderRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersByStatus(String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus);
    }

    // New method: Get all orders for a specific customer
    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersForCustomer(String username) {
        return orderRepository.findByUser_Username(username);
    }

    // New method: Get a specific order for a customer by order number
    @Transactional(readOnly = true)
    public CustomerOrder getOrderForCustomerByOrderNumber(String username, String orderNumber) {
        return orderRepository.findByOrderNumberAndUser_Username(orderNumber, username)
                .orElseThrow(() -> new RuntimeException("Order not found for user " + username + " with order number: " + orderNumber));
    }
}
