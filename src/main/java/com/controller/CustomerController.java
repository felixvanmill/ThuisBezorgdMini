// CustomerController.java

package com.controller;

import com.model.*;
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

    // ✅ Serve customer home page on /customer/home
    @GetMapping("/home")
    public String customerHome(final Model model) {
        model.addAttribute("welcomeMessage", "Welcome to the Customer Dashboard!");
        return "customer/customer"; // Renders templates/customer/customer.html
    }

    // ✅ Show all available restaurants
    @GetMapping("/restaurants")
    public String getAllRestaurants(final Model model) {
        final List<Restaurant> restaurants = this.restaurantRepository.findAll();
        model.addAttribute("restaurants", restaurants);
        return "customer/restaurants";  // Renders the restaurant selection view
    }

    // ✅ Show menu items for a specific restaurant by slug
    @GetMapping("/restaurant/{slug}/menu")
    public String getMenuItemsBySlug(@PathVariable final String slug, final Model model) {
        // Find restaurant by slug
        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        // Fetch menu items with inventory > 0
        final List<MenuItem> menuItems = this.menuItemRepository.findByRestaurant_Id(restaurant.getId());
        menuItems.removeIf(menuItem -> 0 >= menuItem.getInventory()); // Remove items with zero inventory

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantId", restaurant.getId());
        model.addAttribute("restaurantName", restaurant.getName());
        model.addAttribute("restaurantSlug", restaurant.getSlug()); // Add slug to model

        return "customer/menu";  // Renders the menu view
    }

    // ✅ Submit an order
    @PostMapping("/restaurant/{slug}/order")
    public String submitOrder(@PathVariable final String slug,
                              @RequestParam final Map<String, String> menuItemQuantities,
                              final Model model) {
        // Find the restaurant by slug
        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        // Authentication and user retrieval
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();
        final AppUser customer = this.appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Parse the menuItemQuantities Map
        final List<OrderItem> orderItems = new ArrayList<>();
        final List<String> inventoryErrors = new ArrayList<>();

        for (final Map.Entry<String, String> entry : menuItemQuantities.entrySet()) {
            try {
                final Long menuItemId = Long.parseLong(entry.getKey().replaceAll("[^\\d]", ""));  // Ensure key is numeric
                final int quantity = Integer.parseInt(entry.getValue());

                if (0 < quantity) {  // Only add items with quantity greater than 0
                    final MenuItem menuItem = this.menuItemRepository.findById(menuItemId)
                            .orElseThrow(() -> new RuntimeException("Menu item not found for ID: " + menuItemId));

                    if (menuItem.getInventory() < quantity) {
                        inventoryErrors.add("Not enough inventory for: " + menuItem.getName() +
                                " (Available: " + menuItem.getInventory() + ", Requested: " + quantity + ")");
                        continue;
                    }

                    // Reduce inventory temporarily
                    menuItem.reduceInventory(quantity);
                    this.menuItemRepository.save(menuItem);

                    orderItems.add(new OrderItem(menuItem, quantity, null)); // Initialize with null for order number
                }
            } catch (final NumberFormatException e) {
                System.out.println("Skipping invalid key or quantity: " + entry);
            }
        }

        // Check if there were inventory errors
        if (!inventoryErrors.isEmpty()) {
            model.addAttribute("errorMessage", "Some items could not be ordered due to insufficient inventory:");
            model.addAttribute("inventoryErrors", inventoryErrors);

            final List<MenuItem> menuItems = this.menuItemRepository.findByRestaurant_Id(restaurant.getId());
            menuItems.removeIf(menuItem -> 0 >= menuItem.getInventory());

            model.addAttribute("menuItems", menuItems);
            model.addAttribute("restaurantId", restaurant.getId());
            model.addAttribute("restaurantName", restaurant.getName());
            model.addAttribute("restaurantSlug", restaurant.getSlug());

            return "customer/menu";  // Return to the menu page with error messages
        }

        // Calculate the total price directly
        final double totalPrice = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();

        // Retrieve the address associated with the customer
        final Address customerAddress = customer.getAddress(); // Ensure AppUser has a getAddress method

        // Use the existing address when creating the order
        final CustomerOrder newOrder = new CustomerOrder(
                customer,
                orderItems,
                customerAddress,
                OrderStatus.UNCONFIRMED,
                totalPrice,
                restaurant
        );

        // Set order number for each OrderItem
        orderItems.forEach(orderItem -> orderItem.setOrderNumber(newOrder.getOrderNumber()));

        this.customerOrderRepository.save(newOrder);

        // Add orderNumber to the model for display in confirmation
        model.addAttribute("orderNumber", newOrder.getOrderNumber());
        model.addAttribute("message", "Your order has been placed successfully!");
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("restaurantName", restaurant.getName());

        return "customer/orderConfirmation";  // Confirmation view after placing order
    }
}
