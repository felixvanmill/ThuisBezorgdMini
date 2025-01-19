// src/test/java/com/controller/OrderControllerIT.java

package com.controller;

import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerOrderRepository orderRepository;

    /**
     * Integration test for retrieving all orders.
     * Ensures the API returns a list of orders.
     */
    @Test
    void testGetAllOrders() {
        final ResponseEntity<CustomerOrder[]> response = this.restTemplate.getForEntity("/api/orders", CustomerOrder[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    /**
     * Integration test for creating a new order.
     * Ensures the API can create and return a new order.
     */
    @Test
    void testCreateOrder() {
        final CustomerOrder order = new CustomerOrder();
        final ResponseEntity<CustomerOrder> response = this.restTemplate.postForEntity("/api/orders", order, CustomerOrder.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
