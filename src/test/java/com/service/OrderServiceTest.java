// src/test/java/com/service/OrderServiceTest.java

package com.service;

import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @InjectMocks
    private OrderService orderService;

    public OrderServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test retrieving an order by its ID.
     * Ensures the service returns the correct order.
     */
    @Test
    void testGetOrderById() {
        // Arrange
        CustomerOrder order = new CustomerOrder();
        when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        Optional<CustomerOrder> result = orderService.getOrderById(1L);

        // Assert
        assertTrue(result.isPresent(), "Order should be present");
        assertEquals(order, result.get(), "Returned order should match the expected order");
    }

    /**
     * Test retrieving a non-existing order.
     * Ensures an exception is thrown when the order is not found.
     */
    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        when(customerOrderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        Optional<CustomerOrder> result = orderService.getOrderById(1L);
        assertFalse(result.isPresent(), "Order should not be present");
    }

    /**
     * Test creating a new order.
     * Ensures the service can save and return a new order.
     */
    @Test
    void testAddOrder() {
        // Arrange
        CustomerOrder order = new CustomerOrder();
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(order);

        // Act
        CustomerOrder result = orderService.addOrder(order);

        // Assert
        assertNotNull(result, "Created order should not be null");
        assertEquals(order, result, "Returned order should match the created order");
    }

    /**
     * Test updating an order's status.
     * Ensures the status is updated successfully.
     */
    @Test
    void testUpdateOrderStatus() {
        // Arrange
        CustomerOrder order = new CustomerOrder();
        when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(order);

        // Act
        CustomerOrder result = orderService.updateOrderStatus(1L, "DELIVERED");

        // Assert
        assertNotNull(result, "Updated order should not be null");
        assertEquals("DELIVERED", result.getStatus().name(), "Order status should be updated to DELIVERED");
    }
}
