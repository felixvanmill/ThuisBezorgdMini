//package com.service;
//
//import com.dto.CustomerOrderDTO;
//import com.model.CustomerOrder;
//import com.model.OrderStatus;
//import com.repository.CustomerOrderRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class DeliveryServiceTest {
//
//    @Mock
//    private CustomerOrderRepository customerOrderRepository;
//
//    @Mock
//    private SecurityContext securityContext;
//
//    @Mock
//    private Authentication authentication;
//
//    @InjectMocks
//    private DeliveryService deliveryService;
//
//    private CustomerOrder order;
//
//    @BeforeEach
//    void setUp() {
//        order = new CustomerOrder();
//        order.setOrderNumber("ORDER123");
//        order.setDeliveryPerson("deliveryUser");
//        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
//        lenient().when(customerOrderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());
//        lenient().when(customerOrderRepository.findById(anyLong())).thenReturn(Optional.empty());
//        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
//        lenient().when(authentication.getName()).thenReturn("deliveryUser");
//
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    private void mockAuthenticatedUser(String username) {
//        SecurityContextHolder.setContext(securityContext);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getName()).thenReturn(username);
//    }
//
//    @Test
//    void testGetAllDeliveryOrders() {
//        when(customerOrderRepository.findByStatusesWithDetails(any())).thenReturn(List.of(order));
//        List<CustomerOrderDTO> orders = deliveryService.getAllDeliveryOrders();
//        assertEquals(1, orders.size());
//    }
//
//    @Test
//    void testAssignOrder() {
//        mockAuthenticatedUser("deliveryUser");
//        when(customerOrderRepository.findByOrderNumber("ORDER123")).thenReturn(Optional.of(order));
//        when(customerOrderRepository.save(order)).thenReturn(order);
//
//        Map<String, Object> result = deliveryService.assignOrder("ORDER123"); // ✅ Expect a Map
//
//        assertEquals("Delivery person assigned successfully.", result.get("message"));
//        assertTrue(result.containsKey("orderId")); // ✅ Check if orderId is in the Map
//    }
//
//
//
//    @Test
//    void testFindOrderByIdentifier_OrderNotFoundById() {
//        when(customerOrderRepository.findById(1L)).thenReturn(Optional.empty());
//        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.getOrderDetails("1"));
//        assertEquals("Order not found with ID: 1", exception.getMessage());
//    }
//
//    @Test
//    void testFindOrderByIdentifier_OrderNotFoundByOrderNumber() {
//        when(customerOrderRepository.findByOrderNumber("ORDER999")).thenReturn(Optional.empty());
//        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.getOrderDetails("ORDER999"));
//        assertEquals("Order not found with order number: ORDER999", exception.getMessage());
//    }
//
//    @Test
//    void testFindOrderByIdentifier_NumericOrderID_NotFound() {
//        when(customerOrderRepository.findById(99L)).thenReturn(Optional.empty());
//        Exception exception = assertThrows(RuntimeException.class, () -> deliveryService.getOrderDetails("99"));
//        assertEquals("Order not found with ID: 99", exception.getMessage());
//    }
//
//    @Test
//    void testFindOrderByIdentifier_ValidNumericOrderID() {
//        when(customerOrderRepository.findById(100L)).thenReturn(Optional.of(order));
//
//        CustomerOrderDTO result = deliveryService.getOrderDetails("100"); // ✅ Expect DTO
//        assertNotNull(result);
//        assertEquals(order.getOrderNumber(), result.getOrderNumber());
//    }
//
//
//    @Test
//    void testFindOrderByIdentifier_ValidOrderNumber() {
//        when(customerOrderRepository.findByOrderNumber(anyString()))
//                .thenReturn(Optional.of(order)); //
//
//
//        CustomerOrderDTO result = deliveryService.getOrderDetails("ORDER456"); // ✅ Expect DTO
//        assertNotNull(result);
//        assertEquals(order.getOrderNumber(), result.getOrderNumber());
//    }
//
//
//    @Test
//    void testGetAssignedOrders() {
//        mockAuthenticatedUser("deliveryUser");
//
//        when(customerOrderRepository.findByDeliveryPersonAndStatuses(eq("deliveryUser"), anyList()))
//                .thenReturn(List.of(order));
//
//        List<CustomerOrderDTO> result = deliveryService.getAssignedOrders();
//        assertEquals(1, result.size());
//    }
//
//    @Test
//    void testGetDeliveryHistory() {
//        mockAuthenticatedUser("deliveryUser");
//
//        when(customerOrderRepository.findByDeliveryPersonAndStatuses(eq("deliveryUser"), anyList()))
//                .thenReturn(List.of(order));
//
//        List<CustomerOrderDTO> result = deliveryService.getDeliveryHistory();
//        assertEquals(1, result.size());
//    }
//
//
//}
