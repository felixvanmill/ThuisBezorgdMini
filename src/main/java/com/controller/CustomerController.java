// CustomerController.java

package com.controller;

import com.model.CustomerOrder;
import com.model.MenuItem;
import com.model.OrderItem;
import com.model.Restaurant;
import com.model.AppUser;
import com.repository.AppUserRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.model.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    // Serve customer home page on /customer/home
    @GetMapping("/home")
    public String customerHome(Model model) {
        model.addAttribute("welcomeMessage", "Welcome to the Customer Dashboard!");
        return "customer/customer"; // Renders templates/customer/customer.html
    }

    // Show all available restaurants
    @GetMapping("/restaurants")
    public String getAllRestaurants(Model model) {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        model.addAttribute("restaurants", restaurants);
        return "customer/restaurants";  // Renders the restaurant selection view
    }

    // Show menu items for a specific restaurant
    @GetMapping("/restaurant/{restaurantId}/menu")
    public String getMenuItems(@PathVariable Long restaurantId, Model model) {
        List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurantId);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("restaurantName", restaurantRepository.findById(restaurantId).orElse(new Restaurant()).getName());
        return "customer/menu";  // Renders the menu view
    }

    @PostMapping("/restaurant/{restaurantId}/order")
    public String submitOrder(@PathVariable Long restaurantId,
                              @RequestParam Map<String, String> menuItemQuantities,
                              Model model) {
        // Authentication and user retrieval
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        AppUser customer = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Parse the menuItemQuantities Map
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<String, String> entry : menuItemQuantities.entrySet()) {
            try {
                Long menuItemId = Long.parseLong(entry.getKey().replaceAll("[^\\d]", ""));  // Ensure key is numeric
                int quantity = Integer.parseInt(entry.getValue());

                if (quantity > 0) {  // Only add items with quantity greater than 0
                    MenuItem menuItem = menuItemRepository.findById(menuItemId)
                            .orElseThrow(() -> new RuntimeException("Menu item not found for ID: " + menuItemId));

                    orderItems.add(new OrderItem(menuItem, quantity, null)); // Initialize with null for order number
                }
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid key or quantity: " + entry);
            }
        }

        // Calculate the total price directly
        double totalPrice = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();

        // Create and save the new order with totalPrice included in the constructor
        CustomerOrder newOrder = new CustomerOrder(customer, orderItems, new Address(), "UNCONFIRMED", totalPrice, restaurantRepository.findById(restaurantId).orElseThrow());

        // Set order number for each OrderItem
        orderItems.forEach(orderItem -> orderItem.setOrderNumber(newOrder.getOrderNumber()));

        customerOrderRepository.save(newOrder);

        // Add orderNumber to the model for display in confirmation
        model.addAttribute("orderNumber", newOrder.getOrderNumber());
        model.addAttribute("message", "Your order has been placed successfully!");
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("restaurantName", newOrder.getRestaurant().getName());

        return "customer/orderConfirmation";  // Confirmation view after placing order
    }
}
