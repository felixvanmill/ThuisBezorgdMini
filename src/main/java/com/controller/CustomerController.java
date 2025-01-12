package com.controller;

import com.dto.RestaurantDTO;
import com.model.*;
import com.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.repository.MenuItemRepository;
import com.repository.CustomerOrderRepository;
import com.repository.AppUserRepository;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private RestaurantService restaurantService;

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
    public ResponseEntity<?> customerHome() {
        return ResponseEntity.ok(Map.of("welcomeMessage", "Welcome to the Customer Dashboard!"));
    }

    @GetMapping("/restaurants")
    public ResponseEntity<?> getAllRestaurants() {
        List<RestaurantDTO> restaurants = restaurantService.getAllRestaurantsWithMenu();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/restaurant/{slug}/menu")
    public ResponseEntity<?> getMenuItemsBySlug(@PathVariable String slug) {
        try {
            RestaurantDTO restaurantDTO = restaurantService.getRestaurantWithMenu(slug);
            return ResponseEntity.ok(restaurantDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", e.getMessage()));
        }
    }

    @PostMapping("/restaurant/{slug}/order")
    @Transactional
    public ResponseEntity<?> submitOrder(@PathVariable String slug,
                                         @RequestBody Map<Long, Integer> menuItemQuantities) {
        StringBuilder debugLogs = new StringBuilder();
        try {
            debugLogs.append("Processing order for restaurant slug: ").append(slug).append("\n");
            debugLogs.append("Received menu item quantities: ").append(menuItemQuantities).append("\n");

            Restaurant restaurant = validateRestaurant(slug, debugLogs);
            AppUser customer = validateCustomer(debugLogs);
            validateCustomerAddress(customer, debugLogs);

            List<OrderItem> orderItems = new ArrayList<>();
            List<String> inventoryErrors = processOrderItems(menuItemQuantities, orderItems, debugLogs);

            if (!inventoryErrors.isEmpty()) {
                debugLogs.append("Inventory errors: ").append(inventoryErrors).append("\n");
                return ResponseEntity.badRequest().body(Map.of(
                        "errorMessage", "Some items could not be ordered due to inventory issues.",
                        "inventoryErrors", inventoryErrors,
                        "debugLogs", debugLogs.toString()
                ));
            }

            double totalPrice = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
            CustomerOrder newOrder = createOrder(customer, restaurant, orderItems, totalPrice);
            debugLogs.append("Order created successfully. Order Number: ").append(newOrder.getOrderNumber()).append("\n");

            return ResponseEntity.ok(Map.of(
                    "orderNumber", newOrder.getOrderNumber(),
                    "totalPrice", totalPrice,
                    "restaurantName", restaurant.getName(),
                    "message", "Your order has been placed successfully!",
                    "debugLogs", debugLogs.toString()
            ));

        } catch (Exception ex) {
            debugLogs.append("Error while processing order: ").append(ex.getMessage()).append("\n");
            throw new RuntimeException("Failed to process order. Debug logs: " + debugLogs, ex);
        }
    }


    private Restaurant validateRestaurant(String slug, StringBuilder debugLogs) {
        return restaurantService.getRestaurantBySlug(slug)
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

    private List<String> processOrderItems(Map<Long, Integer> menuItemQuantities, List<OrderItem> orderItems, StringBuilder debugLogs) {
        List<String> inventoryErrors = new ArrayList<>();

        menuItemQuantities.forEach((menuItemId, quantity) -> {
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

    private CustomerOrder createOrder(AppUser customer, Restaurant restaurant, List<OrderItem> orderItems, double totalPrice) {
        Address managedAddress = entityManager.merge(customer.getAddress());
        CustomerOrder order = new CustomerOrder(customer, orderItems, managedAddress, OrderStatus.UNCONFIRMED, totalPrice, restaurant);

        orderItems.forEach(item -> item.setOrderNumber(order.getOrderNumber()));
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
