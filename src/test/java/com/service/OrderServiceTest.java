package com.service;

import com.dto.CustomerOrderDTO;
import com.exception.ResourceNotFoundException;
import com.exception.ValidationException;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CustomerOrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private CustomerOrder order;

    @BeforeEach
    void setUp() {
        order = new CustomerOrder();
        order.setId(1L);
        order.setOrderNumber("ORDER123");
        order.setStatus(OrderStatus.READY_FOR_DELIVERY); // ✅ Fix: Set status to avoid null error
    }

    /** Test: Successfully retrieve an order by ID */
    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findCustomerOrderById(1L)).thenReturn(Optional.of(order));

        CustomerOrder result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository).findCustomerOrderById(1L);
    }

    /** Test: Throws ResourceNotFoundException if order ID does not exist */
    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findCustomerOrderById(2L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderById(2L)
        );

        assertEquals("Order not found with ID: 2", exception.getMessage());
    }

    /** Test: Successfully retrieve an order by numeric ID */
    @Test
    void testGetOrderByIdentifier_SuccessWithId() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        CustomerOrderDTO result = orderService.getOrderByIdentifier("1");

        assertNotNull(result);
        assertEquals("ORDER123", result.getOrderNumber());
        verify(orderRepository).findById(1L);
    }

    /** Test: Successfully retrieve an order by order number */
    @Test
    void testGetOrderByIdentifier_SuccessWithOrderNumber() {
        when(orderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));

        CustomerOrderDTO result = orderService.getOrderByIdentifier("ORDER123");

        assertNotNull(result);
        assertEquals("ORDER123", result.getOrderNumber());
        verify(orderRepository).findByOrderNumber("ORDER123");
    }

    /** Test: Throws ResourceNotFoundException if order ID does not exist */
    @Test
    void testGetOrderByIdentifier_IdNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderByIdentifier("99")
        );

        assertEquals("Order not found with ID: 99", exception.getMessage());
    }

    /** Test: Throws ResourceNotFoundException if order number does not exist */
    @Test
    void testGetOrderByIdentifier_OrderNumberNotFound() {
        when(orderRepository.findByOrderNumber("INVALID_ORDER")).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderByIdentifier("INVALID_ORDER")
        );

        assertEquals("Order not found with order number: INVALID_ORDER", exception.getMessage());
    }

    /** Test: Throws ValidationException when identifier is null */
    @Test
    void testGetOrderByIdentifier_NullIdentifier() {
        Exception exception = assertThrows(ValidationException.class, () ->
                orderService.getOrderByIdentifier(null)
        );

        assertEquals("Order identifier cannot be null or empty.", exception.getMessage());
    }

    /** Test: Throws ValidationException when identifier is empty */
    @Test
    void testGetOrderByIdentifier_EmptyIdentifier() {
        Exception exception = assertThrows(ValidationException.class, () ->
                orderService.getOrderByIdentifier("")
        );

        assertEquals("Order identifier cannot be null or empty.", exception.getMessage());
    }

    /** Test: Throws ResourceNotFoundException for a non-existent order number */
    @Test
    void testGetOrderByIdentifier_InvalidIdFormat() {
        when(orderRepository.findByOrderNumber("ABC123")).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderByIdentifier("ABC123")
        );

        assertEquals("Order not found with order number: ABC123", exception.getMessage());
    }

}
