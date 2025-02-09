package com.service;

import com.model.*;
import com.repository.AppUserRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.RestaurantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Handles customer-related operations such as retrieving menus, placing orders, and tracking orders.
 */
@Service
public class CustomerService {

    private final CustomerOrderRepository customerOrderRepository;
    private final MenuItemRepository menuItemRepository;
    private final AppUserRepository appUserRepository;
    private final RestaurantRepository restaurantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Constructor-based Dependency Injection for required repositories.
     *
     * @param customerOrderRepository The repository handling customer orders.
     * @param menuItemRepository      The repository managing menu items.
     * @param appUserRepository       The repository managing user authentication and details.
     * @param restaurantRepository    The repository for retrieving restaurant details.
     */
    public CustomerService(CustomerOrderRepository customerOrderRepository,
                           MenuItemRepository menuItemRepository,
                           AppUserRepository appUserRepository,
                           RestaurantRepository restaurantRepository) {
        this.customerOrderRepository = customerOrderRepository;
        this.menuItemRepository = menuItemRepository;
        this.appUserRepository = appUserRepository;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Retrieves the menu of a specific restaurant by its slug.
     *
     * @param slug The restaurant's unique slug.
     * @return A list of menu items available at the restaurant.
     * @throws RuntimeException if the restaurant is not found.
     */
    public List<MenuItem> getMenuByRestaurantSlug(String slug) {
        return restaurantRepository.findBySlug(slug)
                .map(Restaurant::getMenuItems)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));
    }

    /**
     * Submits a new order for a given restaurant.
     *
     * @param slug               The restaurant's unique slug.
     * @param menuItemQuantities A map containing menu item IDs and their respective quantities.
     * @return The created customer order.
     * @throws RuntimeException if the restaurant, menu items, or customer data is invalid.
     */
    @Transactional
    public CustomerOrder submitOrder(String slug, Map<Long, Integer> menuItemQuantities) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        AppUser customer = getAuthenticatedCustomer();

        if (customer.getAddress() == null) {
            throw new RuntimeException("Customer address is required to place an order.");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0;

        for (Map.Entry<Long, Integer> entry : menuItemQuantities.entrySet()) {
            MenuItem menuItem = menuItemRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + entry.getKey()));

            int quantity = entry.getValue();
            if (menuItem.getInventory() < quantity) {
                throw new RuntimeException("Not enough stock for item: " + menuItem.getName());
            }

            menuItem.reduceInventory(quantity);
            menuItemRepository.save(menuItem);

            OrderItem orderItem = new OrderItem(menuItem, quantity, null);
            orderItems.add(orderItem);
            totalPrice += orderItem.getTotalPrice();
        }

        Address managedAddress = entityManager.merge(customer.getAddress());
        CustomerOrder order = new CustomerOrder(customer, orderItems, managedAddress, OrderStatus.UNCONFIRMED, totalPrice, restaurant);
        orderItems.forEach(item -> item.setOrderNumber(order.getOrderNumber()));

        return customerOrderRepository.save(order);
    }

    /**
     * Retrieves order details based on the order number.
     *
     * @param orderId The unique order identifier.
     * @return The corresponding customer order.
     * @throws RuntimeException if the order is not found or the user lacks permission.
     */
    public CustomerOrder trackOrder(String orderId) {
        String username = getAuthenticatedUsername();
        return customerOrderRepository.findByOrderNumberAndUser_Username(orderId, username)
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));
    }

    /**
     * Cancels an order if it is still in the "UNCONFIRMED" status.
     *
     * @param orderNumber The unique order number.
     * @throws RuntimeException if the order is not found, unauthorized, or cannot be canceled.
     */
    @Transactional
    public void cancelOrder(String orderNumber) {
        String username = getAuthenticatedUsername();
        CustomerOrder order = customerOrderRepository.findByOrderNumberAndUser_Username(orderNumber, username)
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

        if (order.getStatus() != OrderStatus.UNCONFIRMED) {
            throw new RuntimeException("Order cannot be canceled in its current status.");
        }

        order.setStatus(OrderStatus.CANCELED);
        customerOrderRepository.save(order);
    }

    /**
     * Retrieves the username of the currently authenticated user.
     *
     * @return The username of the authenticated customer.
     */
    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Retrieves the authenticated customer from the database.
     *
     * @return The authenticated AppUser entity.
     * @throws RuntimeException if the user cannot be found.
     */
    private AppUser getAuthenticatedCustomer() {
        return appUserRepository.findByUsername(getAuthenticatedUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
    }
}
