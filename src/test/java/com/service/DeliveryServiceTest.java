package com.service;

import com.dto.CustomerOrderDTO;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import com.repository.CustomerOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DeliveryService deliveryService;

    private CustomerOrder order;

    @BeforeEach
    void setUp() {
        order = new CustomerOrder();
        order.setOrderNumber("ORDER123");
        order.setDeliveryPerson("deliveryUser");
        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        lenient().when(customerOrderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());
        lenient().when(customerOrderRepository.findById(anyLong())).thenReturn(Optional.empty());
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("deliveryUser");

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAuthenticatedUser(String username) {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
    }

    @Test
    void testGetAllDeliveryOrders() {
        when(customerOrderRepository.findByStatusesWithDetails(any())).thenReturn(List.of(order));
        List<CustomerOrderDTO> orders = deliveryService.getAllDeliveryOrders();
        assertEquals(1, orders.size());
    }

    @Test
    void testAssignOrder() {
        mockAuthenticatedUser("deliveryUser");
        when(customerOrderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));
        when(customerOrderRepository.save(order)).thenReturn(order);
        CustomerOrder assignedOrder = deliveryService.assignOrder("ORDER123");
        assertEquals("deliveryUser", assignedOrder.getDeliveryPerson());
    }

    @Test
    void testConfirmPickup() {
        mockAuthenticatedUser("deliveryUser");
        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
        when(customerOrderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));
        when(customerOrderRepository.save(order)).thenReturn(order);
        CustomerOrder updatedOrder = deliveryService.confirmPickup("ORDER123");
        assertEquals(OrderStatus.PICKING_UP, updatedOrder.getStatus());
    }

    @Test
    void testConfirmPickup_InvalidStatus() {
        mockAuthenticatedUser("deliveryUser");
        order.setStatus(OrderStatus.DELIVERED);
        when(customerOrderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));
        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.confirmPickup("ORDER123"));
        assertEquals("Order is not in READY_FOR_DELIVERY status.", exception.getMessage());
    }

    @Test
    void testConfirmDelivery() {
        mockAuthenticatedUser("deliveryUser");
        order.setStatus(OrderStatus.TRANSPORT);
        when(customerOrderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));
        when(customerOrderRepository.save(order)).thenReturn(order);
        CustomerOrder deliveredOrder = deliveryService.confirmDelivery("ORDER123");
        assertEquals(OrderStatus.DELIVERED, deliveredOrder.getStatus());
    }

    @Test
    void testConfirmDelivery_InvalidStatus() {
        mockAuthenticatedUser("deliveryUser");
        order.setStatus(OrderStatus.PICKING_UP);
        when(customerOrderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));
        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.confirmDelivery("ORDER123"));
        assertEquals("Order is not in TRANSPORT status.", exception.getMessage());
    }

    @Test
    void testConfirmTransport() {
        mockAuthenticatedUser("deliveryUser");
        order.setStatus(OrderStatus.PICKING_UP);
        when(customerOrderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));
        when(customerOrderRepository.save(order)).thenReturn(order);
        CustomerOrder transportedOrder = deliveryService.confirmTransport("ORDER123");
        assertEquals(OrderStatus.TRANSPORT, transportedOrder.getStatus());
    }

    @Test
    void testConfirmTransport_InvalidStatus() {
        mockAuthenticatedUser("deliveryUser");
        order.setStatus(OrderStatus.DELIVERED);
        when(customerOrderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));
        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.confirmTransport("ORDER123"));
        assertEquals("Order is not in PICKING_UP status.", exception.getMessage());
    }

    @Test
    void testFindOrderByIdentifier_OrderNotFoundById() {
        when(customerOrderRepository.findById(1L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.getOrderDetails("1"));
        assertEquals("Order not found with ID: 1", exception.getMessage());
    }

    @Test
    void testFindOrderByIdentifier_OrderNotFoundByOrderNumber() {
        when(customerOrderRepository.findByOrderNumber("ORDER999")).thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.getOrderDetails("ORDER999"));
        assertEquals("Order not found with order number: ORDER999", exception.getMessage());
    }

    @Test
    void testFindOrderByIdentifier_NumericOrderID_NotFound() {
        when(customerOrderRepository.findById(99L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.getOrderDetails("99"));
        assertEquals("Order not found with ID: 99", exception.getMessage());
    }

    @Test
    void testFindOrderByIdentifier_ValidNumericOrderID() {
        when(customerOrderRepository.findById(100L)).thenReturn(Optional.of(order));
        CustomerOrder result = deliveryService.getOrderDetails("100");
        assertNotNull(result);
    }

    @Test
    void testFindOrderByIdentifier_ValidOrderNumber() {
        when(customerOrderRepository.findByOrderNumber("ORDER456")).thenReturn(Optional.of(order));
        CustomerOrder result = deliveryService.getOrderDetails("ORDER456");
        assertNotNull(result);
    }

    @Test
    void testGetAssignedOrders() {
        mockAuthenticatedUser("deliveryUser");

        when(customerOrderRepository.findByDeliveryPersonAndStatuses(eq("deliveryUser"), anyList()))
                .thenReturn(List.of(order));

        List<CustomerOrderDTO> result = deliveryService.getAssignedOrders();
        assertEquals(1, result.size());
    }

    @Test
    void testGetDeliveryHistory() {
        mockAuthenticatedUser("deliveryUser");

        when(customerOrderRepository.findByDeliveryPersonAndStatuses(eq("deliveryUser"), anyList()))
                .thenReturn(List.of(order));

        List<CustomerOrderDTO> result = deliveryService.getDeliveryHistory();
        assertEquals(1, result.size());
    }

    @Test
    void testValidateDeliveryPerson_ValidUser() {
        mockAuthenticatedUser("deliveryUser");

        order.setDeliveryPerson("deliveryUser");

        assertDoesNotThrow(() -> deliveryService.validateDeliveryPerson(order));
    }

    @Test
    void testValidateDeliveryPerson_InvalidUser() {
        mockAuthenticatedUser("wrongUser");

        order.setDeliveryPerson("deliveryUser");

        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.validateDeliveryPerson(order));
        assertEquals("Unauthorized: You are not assigned to this order.", exception.getMessage());
    }
}
