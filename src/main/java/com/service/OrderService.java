package com.service;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.model.MenuItem;
import com.model.Address;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing orders and related entities.
 */
@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final AddressRepository addressRepository;

    /**
     * Constructor-based Dependency Injection.
     *
     * @param orderRepository    The repository for customer orders.
     * @param menuItemRepository The repository for menu items.
     * @param addressRepository  The repository for addresses.
     */
    public OrderService(CustomerOrderRepository orderRepository,
                        MenuItemRepository menuItemRepository,
                        AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * Get all orders.
     *
     * @return List of all customer orders.
     */
    @Transactional(readOnly = true)
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Get an order by ID using explicit repository method.
     *
     * @param id The ID of the order.
     * @return The order if found.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(Long id) {
        return orderRepository.findCustomerOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
    }

    /**
     * Get an order by order number.
     *
     * @param orderNumber The order number.
     * @return The order if found.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumberWithDetails(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
    }


    /**
     * Add a new order.
     *
     * @param order The order to add.
     * @return The saved order.
     */
    public CustomerOrder addOrder(CustomerOrder order) {
        return orderRepository.save(order);
    }

    /**
     * Update the status of an order.
     *
     * @param id     The ID of the order.
     * @param status The new status.
     * @return The updated order.
     */
    public CustomerOrder updateOrderStatus(Long id, String status) {
        CustomerOrder order = getOrderById(id);
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
            return orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid OrderStatus: " + status);
        }
    }

    /**
     * Delete an order by ID using explicit repository method.
     *
     * @param id The ID of the order.
     */
    public void deleteOrder(Long id) {
        CustomerOrder order = getOrderById(id);
        orderRepository.delete(order);
    }

    /**
     * Get orders by status.
     *
     * @param status The order status.
     * @return List of orders matching the status.
     */
    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get orders for a specific customer by username.
     *
     * @param username The customer's username.
     * @return List of orders belonging to the customer.
     */
    @Transactional(readOnly = true)
    public List<CustomerOrder> getOrdersForCustomer(String username) {
        return orderRepository.findByUser_Username(username);
    }

    /**
     * Get a specific order for a customer by order number.
     *
     * @param username    The customer's username.
     * @param orderNumber The order number.
     * @return The order if found.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderForCustomerByOrderNumber(String username, String orderNumber) {
        return orderRepository.findByOrderNumberAndUser_Username(orderNumber, username)
                .orElseThrow(() -> new RuntimeException("Order not found for user " + username + " with order number: " + orderNumber));
    }

    /**
     * Save or update an order.
     *
     * @param order The order to save or update.
     * @return The saved order.
     */
    public CustomerOrder saveOrder(CustomerOrder order) {
        return orderRepository.save(order);
    }

    /**
     * Fetch a menu item by its ID using the explicit repository method.
     *
     * @param itemId The ID of the menu item.
     * @return The menu item if found.
     */
    @Transactional(readOnly = true)
    public MenuItem getMenuItemById(Long itemId) {
        return menuItemRepository.findMenuItemById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found with ID: " + itemId));
    }

    /**
     * Fetch an address by its ID using the explicit repository method.
     *
     * @param addressId The ID of the address.
     * @return The address if found.
     */
    @Transactional(readOnly = true)
    public Address getAddressById(Long addressId) {
        return addressRepository.findAddressById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));
    }
}
