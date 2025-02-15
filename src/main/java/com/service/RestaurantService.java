// src/main/java/com/service/RestaurantService.java

package com.service;

import com.dto.CustomerOrderDTO;
import com.dto.OrderDTO;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utils.CsvUtils;
import java.nio.charset.StandardCharsets;
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
     * Get a restaurant by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
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
     * Get a restaurant by slug with its employees.
     */
    @Transactional(readOnly = true)
    public Restaurant getRestaurantBySlugWithEmployees(String slug) {
        return restaurantRepository.findBySlugWithEmployees(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));
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

    @Transactional
    public ResponseEntity<?> updateMenuItemAvailability(String username, String slug, Long menuItemId, Map<String, Boolean> request) {
        if (!request.containsKey("isAvailable")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'isAvailable' field."));
        }

        boolean isAvailable = request.get("isAvailable");

        if (!isEmployeeAuthorizedForRestaurant(username, slug)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized access"));
        }

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found."));

        menuItem.setAvailable(isAvailable);
        menuItemRepository.save(menuItem);

        return ResponseEntity.ok(Map.of(
                "message", "Menu item availability updated successfully.",
                "restaurantSlug", slug,
                "menuItemId", menuItem.getId(),
                "menuItemName", menuItem.getName(),
                "newAvailability", menuItem.isAvailable()
        ));
    }


    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadOrdersAsCsv(String username) {
        Long restaurantId = getAuthenticatedRestaurantId(username);
        if (restaurantId == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<OrderDTO> orders = customerOrderRepository.findByRestaurant_IdWithDetails(restaurantId);

        // Generate CSV content using CsvUtils (now includes fetching items)
        String csvContent = CsvUtils.generateCsvFromDTO(orders, customerOrderRepository);
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderDTO> getOrdersForEmployee(String slug, String username) {
        // Fetch the restaurant where the employee works
        Restaurant restaurant = restaurantRepository.findBySlugWithEmployees(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));

        // Check if the employee is authorized
        boolean isEmployee = restaurant.getEmployees().stream()
                .anyMatch(employee -> employee.getUsername().equals(username));

        if (!isEmployee) {
            throw new RuntimeException("User is not an employee of the restaurant.");
        }

        // Fetch and return orders as DTOs
        return customerOrderRepository.findByRestaurant_Id(restaurant.getId())
                .stream().map(CustomerOrderDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderDTO> getOrdersByStatus(OrderStatus orderStatus) {
        return customerOrderRepository.findByStatus(orderStatus)
                .stream().map(CustomerOrderDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderDTO> getAllOrders() {
        return customerOrderRepository.findAll()
                .stream().map(CustomerOrderDTO::new)
                .collect(Collectors.toList());
    }



}
