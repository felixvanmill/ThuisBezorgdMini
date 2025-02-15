package com.service;

import com.dto.CustomerOrderDTO;
import com.dto.MenuItemDTO;
import com.dto.OrderDTO;
import com.dto.RestaurantDTO;
import com.model.*;
import com.repository.AppUserRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.RestaurantRepository;
import com.utils.ResponseUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.utils.AuthUtils.getLoggedInUsername;

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
     * Retrieves the menu of a restaurant by its slug.
     * If the logged-in user is a RESTAURANT_EMPLOYEE, it includes inventory details.
     */
    public List<MenuItemDTO> getMenuByRestaurantSlug(String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        boolean includeInventory = isRestaurantEmployee();

        return restaurant.getMenuItems().stream()
                .map(menuItem -> new MenuItemDTO(menuItem, includeInventory))
                .toList();
    }

    /**
     * Determines if the currently authenticated user is a RESTAURANT_EMPLOYEE.
     */
    private boolean isRestaurantEmployee() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByUsername(username)
                .map(user -> user.getRole().name().equalsIgnoreCase("RESTAURANT_EMPLOYEE"))
                .orElse(false);
    }

    /**
     * Places an order for a given restaurant.
     */
    @Transactional
    public OrderDTO submitOrder(String slug, List<Map<String, Object>> orderItems) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        AppUser customer = getAuthenticatedCustomer();

        if (customer.getAddress() == null) {
            throw new RuntimeException("Customer address is required to place an order.");
        }

        List<OrderItem> createdOrderItems = new ArrayList<>();
        double totalPrice = 0;
        StringBuilder itemsDescription = new StringBuilder(); // ✅ String to store ordered items

        for (Map<String, Object> item : orderItems) {
            Long menuItemId = ((Number) item.get("menuItemId")).longValue();
            Integer quantity = ((Number) item.get("quantity")).intValue();

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + menuItemId));

            if (menuItem.getInventory() < quantity) {
                throw new RuntimeException("Not enough stock for item: " + menuItem.getName());
            }

            menuItem.reduceInventory(quantity);
            menuItemRepository.save(menuItem);

            OrderItem orderItem = new OrderItem(menuItem, quantity, null);
            createdOrderItems.add(orderItem);
            totalPrice += orderItem.getTotalPrice();

            // ✅ Append item details to the description
            itemsDescription.append(quantity)
                    .append("x ")
                    .append(menuItem.getName())
                    .append(", ");
        }

        Address managedAddress = entityManager.merge(customer.getAddress());
        CustomerOrder order = new CustomerOrder(customer, createdOrderItems, managedAddress, OrderStatus.UNCONFIRMED, totalPrice, restaurant);
        createdOrderItems.forEach(item -> item.setOrderNumber(order.getOrderNumber()));
        customerOrderRepository.save(order);

        // ✅ Remove the last comma and space if there are items
        String itemsSummary = itemsDescription.length() > 0
                ? itemsDescription.substring(0, itemsDescription.length() - 2)
                : "No items";

        return new OrderDTO(
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getStatus(),
                customer.getUsername(), // ✅ Username of the customer
                itemsSummary // ✅ Now contains ordered items instead of null
        );
    }



    /**
     * Retrieves order details based on the order number.
     *
     * @param orderId The unique order identifier.
     * @return The corresponding customer order.
     */
    @Transactional
    public CustomerOrderDTO trackOrder(String orderId) {
        String username = getAuthenticatedUsername();
        CustomerOrder order = customerOrderRepository.findByOrderNumberAndUser_Username(orderId, username)
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

        Hibernate.initialize(order.getUser());
        Hibernate.initialize(order.getUser().getAddress());

        return new CustomerOrderDTO(order);
    }

    /**
     * Cancels an order if it is still in the "UNCONFIRMED" status.
     *
     * @param orderNumber The unique order number.
     * @return A confirmation message.
     */
    @Transactional
    public Map<String, String> cancelOrder(String orderNumber) {
        String username = getAuthenticatedUsername();
        CustomerOrder order = customerOrderRepository.findByOrderNumberAndUser_Username(orderNumber, username)
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

        if (order.getStatus() != OrderStatus.UNCONFIRMED) {
            throw new RuntimeException("Order cannot be canceled in its current status.");
        }

        order.setStatus(OrderStatus.CANCELED);
        customerOrderRepository.save(order);

        return Map.of("message", "Order successfully canceled.");
    }

    /**
     * Retrieves a list of all restaurants, including their menu items.
     */
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurants() {
        boolean includeInventory = isRestaurantEmployee();

        List<Restaurant> restaurants = restaurantRepository.findAll();  // Fetch all restaurants
        restaurants.forEach(restaurant -> Hibernate.initialize(restaurant.getMenuItems())); // Ensure menu items are loaded

        return restaurants.stream()
                .map(restaurant -> new RestaurantDTO(restaurant, restaurant.getMenuItems(), includeInventory))
                .collect(Collectors.toList());
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
     */
    private AppUser getAuthenticatedCustomer() {
        return appUserRepository.findByUsername(getAuthenticatedUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
    }

    /**
     * Retrieves all orders for the authenticated customer.
     *
     * @return List of CustomerOrderDTOs representing the orders of the user.
     */
    @Transactional
    public List<CustomerOrderDTO> getAllOrdersForAuthenticatedUser() {
        String username = getAuthenticatedUsername(); // Get logged-in user's username

        List<CustomerOrder> orders = customerOrderRepository.findByUser_Username(username);
        if (orders.isEmpty()) {
            throw new RuntimeException("No orders found for user: " + username);
        }

        return orders.stream().map(CustomerOrderDTO::new).toList();
    }

    /**
     * Updates the status of an order (Customers can only cancel orders).
     *
     * @param orderNumber The unique order number.
     * @param newStatus The requested status update (only "CANCELED" is allowed for customers).
     * @return A confirmation message.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateOrderStatus(String orderNumber, String newStatus) {
        return ResponseUtils.handleRequest(() -> {
            String username = getLoggedInUsername();
            CustomerOrder order = customerOrderRepository.findByOrderNumberAndUser_Username(orderNumber, username)
                    .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

            if (!"CANCELED".equalsIgnoreCase(newStatus)) {
                throw new RuntimeException("Customers can only cancel orders.");
            }

            if (order.getStatus() != OrderStatus.UNCONFIRMED) {
                throw new RuntimeException("Order cannot be canceled in its current status.");
            }

            order.setStatus(OrderStatus.CANCELED);
            customerOrderRepository.save(order);
            return Map.of("message", "Order successfully canceled.");
        });
    }


    /**
     * Retrieves a specific order for the authenticated user.
     */
    public ResponseEntity<CustomerOrderDTO> getOrderByIdentifier(String username, String identifier) {
        return ResponseUtils.handleRequest(() -> {
            if (identifier.matches("\\d+")) { // Numeric order ID
                return customerOrderRepository.findById(Long.parseLong(identifier))
                        .map(CustomerOrderDTO::new)
                        .orElseThrow(() -> new RuntimeException("Order not found"));
            } else { // Alphanumeric order number
                CustomerOrder order = customerOrderRepository.findByOrderNumber(identifier)
                        .orElseThrow(() -> new RuntimeException("Order not found"));

                if (!order.getUser().getUsername().equals(username)) {
                    throw new RuntimeException("Access denied.");
                }

                return new CustomerOrderDTO(order);
            }
        });
    }


}
