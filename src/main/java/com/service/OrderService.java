package com.service;

import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private CustomerOrderRepository orderRepository;

    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<CustomerOrder> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public CustomerOrder addOrder(CustomerOrder order) {
        return orderRepository.save(order);
    }

    public CustomerOrder updateOrderStatus(Long id, String status) {
        Optional<CustomerOrder> orderOptional = orderRepository.findById(id);
        if (orderOptional.isPresent()) {
            CustomerOrder order = orderOptional.get();
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status); // Convert String to Enum
                order.setStatus(orderStatus);
                return orderRepository.save(order);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid OrderStatus: " + status);
            }
        }
        throw new RuntimeException("Order not found with ID: " + id);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
