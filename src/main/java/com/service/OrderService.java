package com.service;

import com.dto.CustomerOrderDTO;
import com.exception.ResourceNotFoundException;
import com.exception.ValidationException;
import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     */
    public OrderService(CustomerOrderRepository orderRepository,
                        MenuItemRepository menuItemRepository,
                        AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.addressRepository = addressRepository;
    }


    /**
     * Get an order by ID.
     */
    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(Long id) {
        return orderRepository.findCustomerOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    /**
     * Get an order by identifier (either ID or order number).
     */
    @Transactional(readOnly = true)
    public CustomerOrderDTO getOrderByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new ValidationException("Order identifier cannot be null or empty.");
        }

        CustomerOrder order;

        // If it's all digits, treat it as an ID. Otherwise, treat it as an order number.
        if (identifier.matches("\\d+")) {
            order = orderRepository.findById(Long.parseLong(identifier))
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + identifier));
        } else {
            order = orderRepository.findByOrderNumber(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + identifier));
        }

        return new CustomerOrderDTO(order);
    }



}

