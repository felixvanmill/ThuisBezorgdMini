package com.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MenuItemControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private String sessionId;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;

        // Add PATCH support for RestTemplate
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        // Prepare form data for login
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Form data payload
        String loginPayload = "username=marysmith&password=password123";

        // Make the login request
        HttpEntity<String> request = new HttpEntity<>(loginPayload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/auth/login", request, String.class);

        // Ensure login is successful
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Extract the JSESSIONID from the Set-Cookie header
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader, "Set-Cookie header is missing");

        // Extract JSESSIONID
        sessionId = setCookieHeader.split(";")[0]; // JSESSIONID=abc123
        assertTrue(sessionId.startsWith("JSESSIONID="), "JSESSIONID is not present in the cookie");
    }

    @Test
    void testUpdateMenuItemAvailability() {
        final Long itemId = 1L;

        // Prepare headers with JSESSIONID
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.COOKIE, sessionId); // Add the session cookie

        // Create the request payload
        HttpEntity<String> request = new HttpEntity<>("{\"isAvailable\": false}", headers);

        // Send the PATCH request
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/restaurant/menu/items/" + itemId + "/availability",
                HttpMethod.PATCH,
                request,
                String.class
        );

        // Assertions to verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Menu item availability updated successfully"));
    }
}
