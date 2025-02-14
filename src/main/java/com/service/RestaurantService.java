// src/main/java/com/service/RestaurantService.java

package com.service;

import com.dto.RestaurantDTO;
import com.model.MenuItem;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.repository.AppUserRepository;
import com.repository.MenuItemRepository;
import com.repository.CustomerOrderRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    /**
     * Get all restaurants.
     */
    @Transactional(readOnly = true)
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Get a restaurant by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    /**
     * Add a new restaurant.
     */
    public Restaurant addRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    /**
     * Delete a restaurant by ID.
     */
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    /**
     * Get all restaurants with menu items (excluding inventory).
     */
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurantsWithMenu() {
        return restaurantRepository.findAll().stream()
                .map(restaurant -> {
                    List<MenuItem> menuItems = menuItemRepository.findByRestaurant_IdAndIsAvailable(restaurant.getId(), true);
                    return new RestaurantDTO(restaurant, menuItems, false); // Exclude inventory
                })
                .collect(Collectors.toList());
    }

    /**
     * Get a restaurant with its menu items.
     */
    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantWithMenu(String slug, boolean includeInventory) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));

        List<MenuItem> menuItems = includeInventory
                ? menuItemRepository.findByRestaurant_Id(restaurant.getId())
                : menuItemRepository.findByRestaurant_IdAndIsAvailable(restaurant.getId(), true);
        System.out.println("Fetched Menu Items in Service: " + menuItems.size());
        return new RestaurantDTO(restaurant, menuItems, includeInventory);
    }

    /**
     * Get restaurant details by slug.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantWithDetailsBySlug(String slug) {
        return restaurantRepository.findBySlugWithDetails(slug);
    }

    /**
     * Get a restaurant associated with an employee by username.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantWithDetailsByEmployeeUsername(String username) {
        return restaurantRepository.findByEmployees_Username(username);
    }

    /**
     * Get a restaurant by slug with its employees.
     */
    @Transactional(readOnly = true)
    public Restaurant getRestaurantBySlugWithEmployees(String slug) {
        return restaurantRepository.findBySlugWithEmployees(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));
    }

    /**
     * Confirm an order for a restaurant.
     */
    @Transactional
    public void confirmOrder(String slug, Long orderId) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getRestaurant().getSlug().equals(slug)) {
            throw new RuntimeException("Order does not belong to the restaurant with slug: " + slug);
        }

        if (order.getStatus() != OrderStatus.UNCONFIRMED) {
            throw new RuntimeException("Order is not in a confirmable state.");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        customerOrderRepository.save(order);
    }

    /**
     * Get orders for a restaurant employee.
     */
    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersForEmployee(String slug, String username) {
        Restaurant restaurant = getRestaurantBySlugWithEmployees(slug);

        boolean isEmployee = restaurant.getEmployees().stream()
                .anyMatch(employee -> employee.getUsername().equals(username));

        if (!isEmployee) {
            throw new RuntimeException("User is not an employee of the restaurant.");
        }

        return customerOrderRepository.findByRestaurant_Id(restaurant.getId());
    }

    /**
     * Get a restaurant by slug.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantBySlug(String slug) {
        return restaurantRepository.findBySlug(slug);
    }

    /**
     * Get an order by ID.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(Long orderId) {
        return customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    /**
     * Get an order by order number.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderByOrderNumber(String orderNumber) {
        return customerOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    /**
     * Save an order.
     */
    @Transactional
    public void saveOrder(CustomerOrder order) {
        customerOrderRepository.save(order);
    }

    /**
     * Check if an employee is authorized for a restaurant.
     */
    @Transactional(readOnly = true)
    public boolean isEmployeeAuthorizedForRestaurant(String username, String restaurantSlug) {
        return restaurantRepository.findBySlugWithEmployees(restaurantSlug)
                .map(restaurant -> restaurant.getEmployees().stream()
                        .anyMatch(employee -> employee.getUsername().equals(username)))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<RestaurantDTO> getRestaurantsForEmployee(String username) {
        Long restaurantId = getAuthenticatedRestaurantId(username);
        if (restaurantId == null) {
            throw new RuntimeException("Employee is not associated with a restaurant.");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        return List.of(new RestaurantDTO(restaurant, restaurant.getMenuItems(), true)); // Include inventory
    }



    private Long getAuthenticatedRestaurantId(String username) {
        return appUserRepository.findByUsername(username)
                .map(user -> user.getRestaurant() != null ? user.getRestaurant().getId() : null)
                .orElse(null);
    }

    @Transactional
    public ResponseEntity<?> updateOrderStatus(String username, String slug, String orderId, Map<String, String> requestBody) {
        if (!isEmployeeAuthorizedForRestaurant(username, slug)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized access"));
        }

        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        CustomerOrder order = customerOrderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Order does not belong to this restaurant."));
        }

        String status = requestBody.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status must be provided."));
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status value."));
        }

        customerOrderRepository.save(order);
        return ResponseEntity.ok(Map.of("message", "Order status updated successfully."));
    }


}
