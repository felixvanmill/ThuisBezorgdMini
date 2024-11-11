package com.service;

import com.model.CustomerOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.repository.CustomerOrderRepository;

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
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return null; // Hier kun je een exception gooien als order niet gevonden wordt.
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
