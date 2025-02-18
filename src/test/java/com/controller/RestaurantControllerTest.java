package com.controller;

import com.dto.CustomerOrderDTO;
import com.response.ApiResponse;
import com.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestaurantControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private String baseUrl;
    private String token;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        // Generate a valid JWT token for marysmith (who is part of the restaurant)
        token = jwtTokenUtil.generateToken("marysmith", "RESTAURANT_EMPLOYEE");
    }


    @Test
    void testGetOrdersForLoggedInEmployee() {
        final String slug = "pizza-place"; // Ensure this restaurant exists in the database

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Fetch raw response for debugging
        ResponseEntity<String> rawResponse = restTemplate.exchange(
                baseUrl + "/api/v1/restaurants/" + slug + "/orders",
                HttpMethod.GET,
                request,
                String.class
        );
        System.out.println("RAW RESPONSE: " + rawResponse.getBody());

        // Ensure response is HTTP 200
        assertEquals(HttpStatus.OK, rawResponse.getStatusCode(), "Expected HTTP 200 OK");
        assertNotNull(rawResponse.getBody(), "Response body should not be null");

        // ✅ Deserialize as ApiResponse<List<CustomerOrderDTO>>
        ResponseEntity<ApiResponse<List<CustomerOrderDTO>>> response = restTemplate.exchange(
                baseUrl + "/api/v1/restaurants/" + slug + "/orders",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().isSuccess(), "Response should indicate success");

        List<CustomerOrderDTO> orders = response.getBody().getData(); // ✅ Extract actual orders
        assertNotNull(orders, "Orders list should not be null");
        assertFalse(orders.isEmpty(), "Orders list should not be empty");

        for (CustomerOrderDTO order : orders) {
            assertNotNull(order.getOrderNumber(), "Order number should not be null");
            assertNotNull(order.getStatus(), "Order status should not be null");
        }
    }

}
