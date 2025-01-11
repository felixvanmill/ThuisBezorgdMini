package com.controller;

import com.model.*;
import com.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

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

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger logger = Logger.getLogger(CustomerController.class.getName());

    @GetMapping("/home")
    public String customerHome(final Model model) {
        model.addAttribute("welcomeMessage", "Welcome to the Customer Dashboard!");
        return "customer/customer";
    }

    @GetMapping("/restaurants")
    public String getAllRestaurants(final Model model) {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        model.addAttribute("restaurants", restaurants);
        return "customer/restaurants";
    }

    @GetMapping("/restaurant/{slug}/menu")
    public String getMenuItemsBySlug(@PathVariable final String slug, final Model model) {
        logger.info("Fetching restaurant menu for slug: " + slug);

        Optional<Restaurant> restaurantOpt = restaurantRepository.findBySlug(slug);

        if (restaurantOpt.isEmpty()) {
            logger.warning("Restaurant not found for slug: " + slug);
            model.addAttribute("errorMessage", "Restaurant not found for slug: " + slug);
            return "customer/error"; // Ensure this template exists.
        }

        Restaurant restaurant = restaurantOpt.get();
        logger.info("Restaurant found: " + restaurant.getName());

        List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());
        if (menuItems.isEmpty()) {
            logger.warning("No menu items available for restaurant: " + restaurant.getName());
        }

        menuItems.removeIf(menuItem -> menuItem.getInventory() <= 0);

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("restaurantSlug", slug);

        logger.info("Successfully loaded menu for restaurant: " + restaurant.getName());
        return "customer/menu";
    }

    @PostMapping("/restaurant/{slug}/order")
    @Transactional
    public String submitOrder(@PathVariable String slug,
                              @RequestParam Map<String, String> menuItemQuantities,
                              Model model) {
        StringBuilder debugLogs = new StringBuilder();
        try {
            debugLogs.append("Processing order for restaurant slug: ").append(slug).append("\n");
            debugLogs.append("Received menu item quantities: ").append(menuItemQuantities).append("\n");

            // Fetch and validate restaurant
            Restaurant restaurant = validateRestaurant(slug, debugLogs);

            // Get authenticated user
            AppUser customer = validateCustomer(debugLogs);

            // Validate customer address
            validateCustomerAddress(customer, debugLogs);

            // Parse menu item IDs from keys like "menuItemQuantities[1]"
            Map<Long, Integer> parsedQuantities = new HashMap<>();
            for (Map.Entry<String, String> entry : menuItemQuantities.entrySet()) {
                String rawKey = entry.getKey();
                String rawValue = entry.getValue();

                // Extract the ID from "menuItemQuantities[<id>]"
                if (rawKey.matches("menuItemQuantities\\[(\\d+)\\]")) {
                    Long menuItemId = Long.parseLong(rawKey.replaceAll("\\D", ""));
                    int quantity = Integer.parseInt(rawValue);
                    parsedQuantities.put(menuItemId, quantity);
                } else {
                    debugLogs.append("Invalid key format: ").append(rawKey).append("\n");
                }
            }

            // Process parsed quantities
            List<OrderItem> orderItems = new ArrayList<>();
            List<String> inventoryErrors = processOrderItems(parsedQuantities, orderItems, debugLogs);

            // Handle inventory errors
            if (!inventoryErrors.isEmpty()) {
                debugLogs.append("Inventory errors: ").append(inventoryErrors).append("\n");
                handleOrderErrors(model, restaurant, slug, inventoryErrors, debugLogs);
                return "customer/menu";
            }

            if (orderItems.isEmpty()) {
                debugLogs.append("No valid items selected for the order.\n");
                throw new RuntimeException("No items selected for the order.");
            }

            // Create and save order
            double totalPrice = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
            CustomerOrder newOrder = createOrder(customer, restaurant, orderItems, totalPrice);
            debugLogs.append("Order created successfully. Order Number: ").append(newOrder.getOrderNumber()).append("\n");

            // Add success attributes to model
            model.addAttribute("orderNumber", newOrder.getOrderNumber());
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("restaurantName", restaurant.getName());
            model.addAttribute("message", "Your order has been placed successfully!");
            model.addAttribute("debugLogs", debugLogs.toString());
            return "customer/orderConfirmation";

        } catch (Exception ex) {
            debugLogs.append("Error while processing order: ").append(ex.getMessage()).append("\n");
            model.addAttribute("errorMessage", "An error occurred while placing your order. Please try again.");
            model.addAttribute("debugLogs", debugLogs.toString());
            return "customer/error";
        }
    }

    private Restaurant validateRestaurant(String slug, StringBuilder debugLogs) {
        return restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    debugLogs.append("Restaurant not found for slug: ").append(slug).append("\n");
                    return new RuntimeException("Restaurant not found for slug: " + slug);
                });
    }

    private AppUser validateCustomer(StringBuilder debugLogs) {
        AppUser customer = getAuthenticatedCustomer();
        debugLogs.append("Authenticated customer: ").append(customer.getUsername()).append("\n");
        return customer;
    }

    private void validateCustomerAddress(AppUser customer, StringBuilder debugLogs) {
        if (customer.getAddress() == null) {
            debugLogs.append("Customer address is missing.\n");
            throw new RuntimeException("Customer address is required to place an order.");
        }
    }

    private List<String> processOrderItems(Map<Long, Integer> parsedQuantities, List<OrderItem> orderItems, StringBuilder debugLogs) {
        List<String> inventoryErrors = new ArrayList<>();

        parsedQuantities.forEach((menuItemId, quantity) -> {
            try {
                if (quantity <= 0) return;

                MenuItem menuItem = menuItemRepository.findById(menuItemId)
                        .orElseThrow(() -> new RuntimeException("Menu item not found for ID: " + menuItemId));

                if (menuItem.getInventory() < quantity) {
                    inventoryErrors.add(menuItem.getName() + " is out of stock (Available: " + menuItem.getInventory() + ").");
                    return;
                }

                menuItem.reduceInventory(quantity);
                menuItemRepository.save(menuItem);
                orderItems.add(new OrderItem(menuItem, quantity, null));
            } catch (Exception e) {
                inventoryErrors.add("Error processing item with ID: " + menuItemId + ". " + e.getMessage());
            }
        });

        return inventoryErrors;
    }

    private void handleOrderErrors(Model model, Restaurant restaurant, String slug, List<String> inventoryErrors, StringBuilder debugLogs) {
        model.addAttribute("errorMessage", "Some items could not be ordered due to inventory issues.");
        model.addAttribute("inventoryErrors", inventoryErrors);

        List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());
        menuItems.removeIf(menuItem -> menuItem.getInventory() <= 0);

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantSlug", slug);
        model.addAttribute("restaurantName", restaurant.getName());
        model.addAttribute("debugLogs", debugLogs.toString());
    }

    private CustomerOrder createOrder(AppUser customer, Restaurant restaurant, List<OrderItem> orderItems, double totalPrice) {
        // Re-fetch or merge Address to ensure it's attached to the session
        Address managedAddress = entityManager.merge(customer.getAddress());

        // Create the order
        CustomerOrder order = new CustomerOrder(customer, orderItems, managedAddress, OrderStatus.UNCONFIRMED, totalPrice, restaurant);

        // Assign order number to order items
        orderItems.forEach(item -> item.setOrderNumber(order.getOrderNumber()));

        // Save the order
        return customerOrderRepository.save(order);
    }

    private AppUser getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found.");
        }

        return appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + authentication.getName()));
    }
}
