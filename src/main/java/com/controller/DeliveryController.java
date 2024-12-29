package com.controller;

import com.model.CustomerOrder;
import com.model.OrderStatus;
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

    // ✅ Serve the allOrders page to display relevant orders
    @GetMapping("/allOrders")
    public String getAllOrders(final Model model) {
        // Fetch orders with relevant statuses
        final List<OrderStatus> statuses = List.of(
                OrderStatus.READY_FOR_DELIVERY,
                OrderStatus.PICKING_UP,
                OrderStatus.TRANSPORT
        );

        final List<CustomerOrder> orders = this.customerOrderRepository.findByStatusesWithDetails(statuses);

        // Add data to the model
        final String username = this.getLoggedInUsername();
        model.addAttribute("username", username);
        model.addAttribute("orders", orders);

        return "delivery/allOrders"; // Maps to templates/delivery/allOrders.html
    }

    // ✅ Update order status action
    @PostMapping("/orders/{orderId}/updateStatus")
    public String updateOrderStatus(@PathVariable final Long orderId, @RequestParam final String status) {
        final CustomerOrder order = this.customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        try {
            final OrderStatus orderStatus = OrderStatus.valueOf(status); // Convert String to Enum
            order.setStatus(orderStatus);
            this.customerOrderRepository.save(order);
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + status);
        }

        return "redirect:/delivery/allOrders";
    }

    // ✅ Utility method to get logged-in username
    private String getLoggedInUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
