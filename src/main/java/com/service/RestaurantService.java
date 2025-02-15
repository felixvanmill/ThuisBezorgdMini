package com.service;

import com.dto.CustomerOrderDTO;
import com.dto.OrderDTO;
import com.dto.RestaurantDTO;
import com.exception.ResourceNotFoundException;
import com.exception.ValidationException;
import com.model.MenuItem;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.repository.AppUserRepository;
import com.repository.MenuItemRepository;
import com.repository.CustomerOrderRepository;
import com.repository.RestaurantRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utils.CsvUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final CustomerOrderRepository customerOrderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final AppUserRepository appUserRepository;

    public RestaurantService(CustomerOrderRepository customerOrderRepository,
                             RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository,
                             AppUserRepository appUserRepository) {
        this.customerOrderRepository = customerOrderRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.appUserRepository = appUserRepository;
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
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for employee: " + username));

        return List.of(new RestaurantDTO(restaurant, restaurant.getMenuItems(), true)); // Include inventory
    }

    private Long getAuthenticatedRestaurantId(String username) {
        return appUserRepository.findByUsername(username)
                .map(user -> user.getRestaurant() != null ? user.getRestaurant().getId() : null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Transactional
    public Map<String, Object> updateOrderStatus(String username, String slug, String orderId, Map<String, String> requestBody) {
        if (!isEmployeeAuthorizedForRestaurant(username, slug)) {
            throw new ValidationException("Unauthorized access to restaurant: " + slug);
        }

        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for slug: " + slug));

        CustomerOrder order = customerOrderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new ValidationException("Order does not belong to this restaurant.");
        }

        String status = requestBody.get("status");
        if (status == null || status.isBlank()) {
            throw new ValidationException("Status must be provided.");
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid order status value: " + status);
        }

        customerOrderRepository.save(order);
        return Map.of("message", "Order status updated successfully.");
    }

    @Transactional
    public Map<String, Object> updateMenuItemAvailability(String username, String slug, Long menuItemId, Map<String, Boolean> request) {
        if (!request.containsKey("isAvailable")) {
            throw new ValidationException("Missing 'isAvailable' field.");
        }

        boolean isAvailable = request.get("isAvailable");

        if (!isEmployeeAuthorizedForRestaurant(username, slug)) {
            throw new ValidationException("Unauthorized access to restaurant: " + slug);
        }

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + menuItemId));

        menuItem.setAvailable(isAvailable);
        menuItemRepository.save(menuItem);

        return Map.of(
                "message", "Menu item availability updated successfully.",
                "restaurantSlug", slug,
                "menuItemId", menuItem.getId(),
                "menuItemName", menuItem.getName(),
                "newAvailability", menuItem.isAvailable()
        );
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadOrdersAsCsv(String username) {
        Long restaurantId = getAuthenticatedRestaurantId(username);
        List<OrderDTO> orders = customerOrderRepository.findByRestaurant_IdWithDetails(restaurantId);

        String csvContent = CsvUtils.generateCsvFromDTO(orders, customerOrderRepository);
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderDTO> getOrdersForEmployee(String slug, String username) {
        Restaurant restaurant = restaurantRepository.findBySlugWithEmployees(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with slug: " + slug));

        boolean isEmployee = restaurant.getEmployees().stream()
                .anyMatch(employee -> employee.getUsername().equals(username));

        if (!isEmployee) {
            throw new ValidationException("User is not an employee of the restaurant.");
        }

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
