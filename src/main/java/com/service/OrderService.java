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
        return this.orderRepository.findAll();
    }

    public Optional<CustomerOrder> getOrderById(final Long id) {
        return this.orderRepository.findById(id);
    }

    public CustomerOrder addOrder(final CustomerOrder order) {
        return this.orderRepository.save(order);
    }

    public CustomerOrder updateOrderStatus(final Long id, final String status) {
        final Optional<CustomerOrder> orderOptional = this.orderRepository.findById(id);
        if (orderOptional.isPresent()) {
            final CustomerOrder order = orderOptional.get();
            try {
                final OrderStatus orderStatus = OrderStatus.valueOf(status); // Convert String to Enum
                order.setStatus(orderStatus);
                return this.orderRepository.save(order);
            } catch (final IllegalArgumentException e) {
                throw new RuntimeException("Invalid OrderStatus: " + status);
            }
        }
        throw new RuntimeException("Order not found with ID: " + id);
    }

    public void deleteOrder(final Long id) {
        this.orderRepository.deleteById(id);
    }
}
