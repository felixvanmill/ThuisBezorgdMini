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

/**
 * Controller for managing customer-specific actions.
 * Provides functionality for browsing restaurants, viewing menus, and placing orders.
 */
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

    /**
     * Serve the customer home page.
     *
     * @param model The model to pass attributes to the view.
     * @return The view for the customer dashboard.
     */
    @GetMapping("/home")
    public String customerHome(final Model model) {
        model.addAttribute("welcomeMessage", "Welcome to the Customer Dashboard!");
        return "customer/customer"; // Renders templates/customer/customer.html
    }

    /**
     * Display all available restaurants.
     *
     * @param model The model to pass attributes to the view.
     * @return The view for the restaurant list.
     */
    @GetMapping("/restaurants")
    public String getAllRestaurants(final Model model) {
        final List<Restaurant> restaurants = this.restaurantRepository.findAll();
        model.addAttribute("restaurants", restaurants);
        return "customer/restaurants"; // Renders the restaurant selection view
    }

    /**
     * Show menu items for a specific restaurant identified by its slug.
     *
     * @param slug  The slug of the restaurant.
     * @param model The model to pass attributes to the view.
     * @return The view displaying the restaurant menu.
     */
    @GetMapping("/restaurant/{slug}/menu")
    public String getMenuItemsBySlug(@PathVariable final String slug, final Model model) {
        // Find the restaurant by slug
        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        // Fetch menu items with inventory > 0
        final List<MenuItem> menuItems = this.menuItemRepository.findByRestaurant_Id(restaurant.getId());
        menuItems.removeIf(menuItem -> 0 >= menuItem.getInventory()); // Remove out-of-stock items

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantId", restaurant.getId());
        model.addAttribute("restaurantName", restaurant.getName());
        model.addAttribute("restaurantSlug", restaurant.getSlug()); // Add slug to model

        return "customer/menu"; // Renders the menu view
    }

    /**
     * Submit an order for a specific restaurant.
     *
     * @param slug               The slug of the restaurant.
     * @param menuItemQuantities A map of menu item IDs to quantities.
     * @param model              The model to pass attributes to the view.
     * @return The view displaying the order confirmation or returning to the menu with errors.
     */
    @PostMapping("/restaurant/{slug}/order")
    public String submitOrder(@PathVariable final String slug,
                              @RequestParam final Map<String, String> menuItemQuantities,
                              final Model model) {
        // Find the restaurant by slug
        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        // Retrieve the authenticated user
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();
        final AppUser customer = this.appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Parse the menuItemQuantities map and validate inventory
        final List<OrderItem> orderItems = new ArrayList<>();
        final List<String> inventoryErrors = new ArrayList<>();

        for (final Map.Entry<String, String> entry : menuItemQuantities.entrySet()) {
            try {
                // Parse menu item ID and quantity from the input map
                final Long menuItemId = Long.parseLong(entry.getKey().replaceAll("[^\\d]", ""));
                final int quantity = Integer.parseInt(entry.getValue());

                if (quantity > 0) { // Only process valid quantities
                    final MenuItem menuItem = this.menuItemRepository.findById(menuItemId)
                            .orElseThrow(() -> new RuntimeException("Menu item not found for ID: " + menuItemId));

                    // Validate inventory
                    if (menuItem.getInventory() < quantity) {
                        inventoryErrors.add("Not enough inventory for: " + menuItem.getName() +
                                " (Available: " + menuItem.getInventory() + ", Requested: " + quantity + ")");
                        continue;
                    }

                    // Temporarily reduce inventory
                    menuItem.reduceInventory(quantity);
                    this.menuItemRepository.save(menuItem);

                    // Add the order item
                    orderItems.add(new OrderItem(menuItem, quantity, null)); // Order number is set later
                }
            } catch (final NumberFormatException e) {
                System.out.println("Skipping invalid key or quantity: " + entry);
            }
        }

        // Handle inventory errors
        if (!inventoryErrors.isEmpty()) {
            model.addAttribute("errorMessage", "Some items could not be ordered due to insufficient inventory:");
            model.addAttribute("inventoryErrors", inventoryErrors);

            // Reload menu for the restaurant
            final List<MenuItem> menuItems = this.menuItemRepository.findByRestaurant_Id(restaurant.getId());
            menuItems.removeIf(menuItem -> 0 >= menuItem.getInventory());

            model.addAttribute("menuItems", menuItems);
            model.addAttribute("restaurantId", restaurant.getId());
            model.addAttribute("restaurantName", restaurant.getName());
            model.addAttribute("restaurantSlug", restaurant.getSlug());

            return "customer/menu"; // Return to menu with errors
        }

        // Calculate the total price
        final double totalPrice = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();

        // Retrieve the customer's address for delivery
        final Address customerAddress = customer.getAddress();

        // Create and save the new order
        final CustomerOrder newOrder = new CustomerOrder(
                customer,
                orderItems,
                customerAddress,
                OrderStatus.UNCONFIRMED,
                totalPrice,
                restaurant
        );

        // Assign order number to each order item
        orderItems.forEach(orderItem -> orderItem.setOrderNumber(newOrder.getOrderNumber()));

        this.customerOrderRepository.save(newOrder);

        // Pass success message and order details to the model
        model.addAttribute("orderNumber", newOrder.getOrderNumber());
        model.addAttribute("message", "Your order has been placed successfully!");
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("restaurantName", restaurant.getName());

        return "customer/orderConfirmation"; // Confirmation view
    }
}
