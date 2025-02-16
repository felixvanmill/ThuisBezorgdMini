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
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @Mock
//    private CustomerOrderRepository customerOrderRepository;
//
//    @InjectMocks
//    private OrderService orderService;
//
//    private CustomerOrder order;
//
//    @BeforeEach
//    void setUp() {
//        order = new CustomerOrder();
//        order.setId(1L);
//        order.setOrderNumber("ORDER123");
//        order.setStatus(OrderStatus.READY_FOR_DELIVERY);
//
//        lenient().doReturn(Optional.of(order)).when(customerOrderRepository).findCustomerOrderById(1L);
//    }
//
//    @Test
//    void testGetOrderById_Success() {
//        CustomerOrder result = orderService.getOrderById(1L);
//        assertNotNull(result);
//        assertEquals(1L, result.getId());
//    }
//
//    @Test
//    void testUpdateOrderStatus_Success() {
//        when(customerOrderRepository.findCustomerOrderById(1L)).thenReturn(Optional.of(order));
//        when(customerOrderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        CustomerOrder result = orderService.updateOrderStatus(1L, "DELIVERED");
//
//        assertNotNull(result);
//        assertEquals(OrderStatus.DELIVERED, result.getStatus());
//        verify(customerOrderRepository).save(order);
//    }
//}
