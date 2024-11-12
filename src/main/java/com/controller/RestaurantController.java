package com.controller;

import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @GetMapping("/orders")
    public List<CustomerOrder> getOrders() {
        return customerOrderRepository.findAll();  // Restaurant employees can view all orders
    }

    @PostMapping("/updateOrderStatus")
    public String updateOrderStatus(@RequestBody CustomerOrder order) {
        customerOrderRepository.save(order);
        return "Order status updated!";
    }
}
