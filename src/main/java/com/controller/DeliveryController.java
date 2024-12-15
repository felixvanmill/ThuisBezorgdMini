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

    // Serve the allOrders page to display relevant orders
    @GetMapping("/allOrders")
    public String getAllOrders(Model model) {
        // Fetch orders with relevant statuses
        List<String> statuses = List.of("READY FOR DELIVERY", "PICKING UP", "TRANSPORT");
        List<CustomerOrder> orders = customerOrderRepository.findByStatusesWithDetails(statuses);

        // Add data to the model
        String username = getLoggedInUsername();
        model.addAttribute("username", username);
        model.addAttribute("orders", orders);

        return "delivery/allOrders"; // Maps to templates/delivery/allOrders.html
    }

    // Update order status action
    @PostMapping("/orders/{orderId}/updateStatus")
    public String updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        // Fetch the order and update its status
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(status);
        customerOrderRepository.save(order);

        // Redirect back to the allOrders view
        return "redirect:/delivery/allOrders";
    }

    // Utility method to get logged-in username
    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
