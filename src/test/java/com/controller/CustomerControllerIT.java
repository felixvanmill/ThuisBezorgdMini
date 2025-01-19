// src/test/java/com/controller/CustomerControllerIT.java

package com.controller;

import com.model.CustomerOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Integration test for the customer home page.
     * Ensures the home page content is correctly returned.
     */
    @Test
    void testGetCustomerHomePage() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/customer/home", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Welcome to the Customer Dashboard"));
    }

    /**
     * Integration test for retrieving all available restaurants.
     * Ensures the restaurants page is correctly returned.
     */
    @Test
    void testViewRestaurants() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/customer/restaurants", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Available Restaurants"));
    }
}
