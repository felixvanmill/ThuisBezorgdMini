package com.controller;

import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Serve the restaurant home page as an HTML view
    @GetMapping("/home")
    public String restaurantHome(Model model) {
        model.addAttribute("welcomeMessage", "Welcome to your restaurant home page!");
        return "restaurant/restaurant"; // Maps to templates/restaurant/restaurant.html
    }

    // View all orders for this restaurant as JSON (for API access)
    @GetMapping("/api/orders/{restaurantId}")
    @ResponseBody
    public List<CustomerOrder> getOrdersForRestaurant(@PathVariable Long restaurantId) {
        return customerOrderRepository.findByRestaurant_Id(restaurantId);
    }

    // Update order status (for API access)
    @PostMapping("/api/orders/updateStatus")
    @ResponseBody
    public String updateOrderStatus(@RequestBody CustomerOrder order) {
        customerOrderRepository.save(order);
        return "Order status updated!";
    }
}
