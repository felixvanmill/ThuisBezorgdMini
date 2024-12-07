package com.controller;

import com.model.CustomerOrder;
import com.model.Restaurant; // Import the Restaurant class
import com.repository.CustomerOrderRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @GetMapping("/home")
    public String restaurantHome(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch the restaurant associated with this employee
        Restaurant restaurant = restaurantRepository.findByEmployees_Username(username)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for user: " + username));

        // Fetch all orders for this restaurant
        List<CustomerOrder> orders = customerOrderRepository.findByRestaurant_Id(restaurant.getId());

        model.addAttribute("username", username);
        model.addAttribute("welcomeMessage", "Welcome to your restaurant home page!");
        model.addAttribute("orders", orders);
        model.addAttribute("restaurantName", restaurant.getName()); // Add the restaurant name to the model

        return "restaurant/restaurant"; // Maps to templates/restaurant/restaurant.html
    }

    @GetMapping("/orders/{orderId}/details")
    public String viewOrderDetails(@PathVariable Long orderId, Model model) {
        // Fetch the order with items eagerly
        CustomerOrder order = customerOrderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        model.addAttribute("order", order);
        model.addAttribute("items", order.getOrderItems());
        return "restaurant/orderDetails";
    }




    @PostMapping("/orders/{orderId}/updateStatus")
    public String updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(status);
        customerOrderRepository.save(order);

        return "redirect:/restaurant/home"; // Redirect to home after updating
    }
}
