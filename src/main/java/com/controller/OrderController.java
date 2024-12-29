package com.controller;

import com.model.CustomerOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.service.OrderService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<CustomerOrder> getAllOrders() {
        return this.orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Optional<CustomerOrder> getOrderById(@PathVariable final Long id) {
        return this.orderService.getOrderById(id);
    }

    @PostMapping
    public CustomerOrder addOrder(@RequestBody final CustomerOrder order) {
        return this.orderService.addOrder(order);
    }

    @PutMapping("/{id}")
    public CustomerOrder updateOrderStatus(@PathVariable final Long id, @RequestParam final String status) {
        return this.orderService.updateOrderStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable final Long id) {
        this.orderService.deleteOrder(id);
    }
}
