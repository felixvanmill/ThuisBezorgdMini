package com.controller;

import com.model.CustomerOrder;
import com.model.Restaurant;
import com.repository.CustomerOrderRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // Serve the customer home page as an HTML view
    @GetMapping("/home")
    public String customerHome(Model model) {
        model.addAttribute("welcomeMessage", "Welcome to your customer home page!");
        return "customer/customer"; // Maps to templates/customer/customer.html
    }

    // Fetch all orders for the customer by username as JSON (for API access)
    @GetMapping("/api/orders")
    @ResponseBody
    public List<CustomerOrder> getCustomerOrders(@RequestParam String username) {
        return customerOrderRepository.findByUser_Username(username);
    }

    // Browse available restaurants as JSON (for API access)
    @GetMapping("/api/restaurants")
    @ResponseBody
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }
}
