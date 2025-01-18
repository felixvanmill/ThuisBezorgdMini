package com.service;

import com.dto.RestaurantDTO;
import com.model.MenuItem;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.repository.MenuItemRepository;
import com.repository.CustomerOrderRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    public Restaurant addRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurantsWithMenu() {
        return restaurantRepository.findAll().stream()
                .map(restaurant -> {
                    List<MenuItem> availableMenuItems = menuItemRepository.findByRestaurant_IdAndIsAvailable(restaurant.getId(), true);
                    return new RestaurantDTO(restaurant, availableMenuItems, false); // Exclude inventory
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantWithMenu(String slug, boolean includeInventory) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));

        List<MenuItem> menuItems;
        if (includeInventory) {
            menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());
        } else {
            menuItems = menuItemRepository.findByRestaurant_IdAndIsAvailable(restaurant.getId(), true);
        }

        return new RestaurantDTO(restaurant, menuItems, includeInventory);
    }

    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantWithDetailsBySlug(String slug) {
        return restaurantRepository.findBySlugWithDetails(slug);
    }

    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantWithDetailsByEmployeeUsername(String username) {
        return restaurantRepository.findByEmployees_Username(username);
    }

    @Transactional(readOnly = true)
    public Restaurant getRestaurantBySlugWithEmployees(String slug) {
        return restaurantRepository.findBySlugWithEmployees(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));
    }

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

    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantBySlug(String slug) {
        return restaurantRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(Long orderId) {
        return customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderByOrderNumber(String orderNumber) {
        return customerOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }

    @Transactional
    public void saveOrder(CustomerOrder order) {
        customerOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public boolean isEmployeeAuthorizedForRestaurant(String username, String restaurantSlug) {
        return restaurantRepository.findBySlugWithEmployees(restaurantSlug)
                .map(restaurant -> restaurant.getEmployees().stream()
                        .anyMatch(employee -> employee.getUsername().equals(username)))
                .orElse(false);
    }
}

