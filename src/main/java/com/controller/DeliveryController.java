package com.controller;

import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Controller
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @GetMapping("/home")
    public String deliveryHome(Model model) {
        // Retrieve authenticated user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Add the username to the model
        model.addAttribute("username", username);
        model.addAttribute("welcomeMessage", "Welcome to your delivery home page!");
        return "delivery/delivery"; // Maps to templates/delivery/delivery.html
    }

    // Get assigned orders with status "ASSIGNED"
    @GetMapping("/api/assignedOrders")
    @ResponseBody
    public List<CustomerOrder> getAssignedOrders() {
        return customerOrderRepository.findByStatus("ASSIGNED");
    }

    // Mark order as delivered (for API access)
    @PostMapping("/api/orders/markDelivered")
    @ResponseBody
    public String markOrderAsDelivered(@RequestParam Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        order.setStatus("DELIVERED");
        customerOrderRepository.save(order);
        return "Order marked as delivered!";
    }

    // Assign an order to a delivery person (for API access)
    @PostMapping("/api/orders/assign")
    @ResponseBody
    public String assignOrderToDelivery(@RequestParam Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        order.setStatus("ASSIGNED");
        customerOrderRepository.save(order);
        return "Order assigned successfully!";
    }
}
