package com.controller;

import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    // Serve the delivery home page as an HTML view
    @GetMapping("/home")
    public String deliveryHome(Model model) {
        model.addAttribute("welcomeMessage", "Welcome to your delivery home page!");
        return "delivery/delivery"; // Maps to templates/delivery/delivery.html
    }

    // Get assigned orders for the delivery person as JSON
    @GetMapping("/api/assignedOrders")
    @ResponseBody
    public List<CustomerOrder> getAssignedOrders(@RequestParam String username) {
        return customerOrderRepository.findByDeliveryPersonUsername(username);
    }

    // Mark order as delivered (for API access)
    @PostMapping("/api/orders/markDelivered")
    @ResponseBody
    public String markOrderAsDelivered(@RequestBody CustomerOrder order) {
        order.setStatus("DELIVERED");
        customerOrderRepository.save(order);
        return "Order marked as delivered!";
    }

    // Assign an order to a delivery person (for API access)
    @PostMapping("/api/orders/assign")
    @ResponseBody
    public String assignOrderToDelivery(@RequestParam Long orderId, @RequestParam String deliveryUsername) {
        CustomerOrder order = customerOrderRepository.findById(orderId).orElseThrow();
        order.setDeliveryPersonUsername(deliveryUsername);
        order.setStatus("ASSIGNED");
        customerOrderRepository.save(order);
        return "Order assigned to " + deliveryUsername;
    }
}
