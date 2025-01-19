package com.controller;

import com.dto.RestaurantDTO;
import com.model.*;
import com.repository.AppUserRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.service.RestaurantService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles customer-related operations like viewing restaurants, placing orders, and tracking orders.
 */
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

    /**
     * Retrieves a list of all restaurants with their menus.
     */
    @GetMapping("/restaurants")
    public ResponseEntity<?> getAllRestaurants() {
        List<RestaurantDTO> restaurants = restaurantService.getAllRestaurantsWithMenu();
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Retrieves the menu of a specific restaurant by its slug.
     */
    @GetMapping("/{slug}/menu")
    public ResponseEntity<?> getMenuBySlug(@PathVariable String slug) {
        try {
            RestaurantDTO restaurantDTO = restaurantService.getRestaurantWithMenu(slug, false); // Exclude inventory details
            return ResponseEntity.ok(restaurantDTO.getMenuItems());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Submits an order for a restaurant with selected menu items and their quantities.
     * Slug turns restaurants name into something that can be easily used in a URL
     * For example: "Sushi Place" will turn into sushi-place
     */
    @PostMapping("/restaurant/{slug}/order")
    @Transactional
    public ResponseEntity<?> submitOrder(@PathVariable String slug, @RequestBody Map<Long, Integer> menuItemQuantities) {
        StringBuilder debugLogs = new StringBuilder();

        try {
            debugLogs.append("Processing order for restaurant: ").append(slug).append("\n");
            debugLogs.append("Menu items: ").append(menuItemQuantities).append("\n");

            // Validate restaurant and customer
            Restaurant restaurant = validateRestaurant(slug, debugLogs);
            AppUser customer = validateCustomer(debugLogs);
            validateCustomerAddress(customer, debugLogs);

            // Process order items and check inventory
            List<OrderItem> orderItems = new ArrayList<>();
            List<String> inventoryErrors = processOrderItems(menuItemQuantities, orderItems, debugLogs);

            // Return error if inventory issues exist
            if (!inventoryErrors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "errorMessage", "Some items could not be ordered due to inventory issues.",
                        "inventoryErrors", inventoryErrors,
                        "debugLogs", debugLogs.toString()
                ));
            }

            // Calculate total price and create the order
            double totalPrice = orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
            CustomerOrder newOrder = createOrder(customer, restaurant, orderItems, totalPrice);

            return ResponseEntity.ok(Map.of(
                    "orderNumber", newOrder.getOrderNumber(),
                    "totalPrice", totalPrice,
                    "restaurantName", restaurant.getName(),
                    "message", "Your order has been placed successfully!",
                    "debugLogs", debugLogs.toString()
            ));

        } catch (Exception ex) {
            debugLogs.append("Error processing order: ").append(ex.getMessage()).append("\n");
            throw new RuntimeException("Order failed. Logs: " + debugLogs, ex);
        }
    }

    /**
     * Tracks the status of a specific order by its ID.
     */
    @GetMapping("/track-order/{orderId}")
    public ResponseEntity<?> trackOrder(@PathVariable String orderId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            CustomerOrder order = customerOrderRepository.findByOrderNumberAndUser_Username(orderId, username)
                    .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Order details fetched successfully",
                    "data", Map.of(
                            "orderNumber", order.getOrderNumber(),
                            "status", order.getStatus().name(),
                            "restaurant", order.getRestaurant().getName(),
                            "deliveryPerson", order.getDeliveryPerson() != null ? order.getDeliveryPerson() : "Not assigned",
                            "estimatedDeliveryTime", "30-45 minutes"
                    )
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * This can cancel, only if its in Unconfirmed status.
     */
    @PostMapping("/orders/{orderNumber}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderNumber) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            CustomerOrder order = customerOrderRepository.findByOrderNumberAndUser_Username(orderNumber, username)
                    .orElseThrow(() -> new RuntimeException("Order not found or access denied."));

            if (OrderStatus.CANCELED == order.getStatus()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Order was already canceled."));
            }
            if (OrderStatus.UNCONFIRMED != order.getStatus()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "Order is in status " + order.getStatus() + " and cannot be canceled."
                ));
            }

            order.setStatus(OrderStatus.CANCELED);
            customerOrderRepository.save(order);

            return ResponseEntity.ok(Map.of("message", "Order successfully canceled."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Helper methods for validation and processing

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
