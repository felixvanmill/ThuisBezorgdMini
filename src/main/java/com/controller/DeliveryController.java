package com.controller;

import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @GetMapping("/assignedOrders")
    public List<CustomerOrder> getAssignedOrders() {
        // Delivery person can get assigned orders based on their username (e.g., stored in the database)
        return customerOrderRepository.findByDeliveryPersonUsername("deliveryPersonUsername");
    }

    @PostMapping("/markDelivered")
    public String markOrderAsDelivered(@RequestBody CustomerOrder order) {
        order.setStatus("DELIVERED");
        customerOrderRepository.save(order);
        return "Order marked as delivered!";
    }
}
