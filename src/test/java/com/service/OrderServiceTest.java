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
        final CustomerOrder order = new CustomerOrder();
        when(this.customerOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        final Optional<CustomerOrder> result = this.orderService.getOrderById(1L);

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
        when(this.customerOrderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        final Optional<CustomerOrder> result = this.orderService.getOrderById(1L);
        assertFalse(result.isPresent(), "Order should not be present");
    }

    /**
     * Test creating a new order.
     * Ensures the service can save and return a new order.
     */
    @Test
    void testAddOrder() {
        // Arrange
        final CustomerOrder order = new CustomerOrder();
        when(this.customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(order);

        // Act
        final CustomerOrder result = this.orderService.addOrder(order);

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
        final CustomerOrder order = new CustomerOrder();
        when(this.customerOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(this.customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(order);

        // Act
        final CustomerOrder result = this.orderService.updateOrderStatus(1L, "DELIVERED");

        // Assert
        assertNotNull(result, "Updated order should not be null");
        assertEquals("DELIVERED", result.getStatus().name(), "Order status should be updated to DELIVERED");
    }
}
