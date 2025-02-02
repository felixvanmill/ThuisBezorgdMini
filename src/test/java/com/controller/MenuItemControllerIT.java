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

        // Add PATCH support for RestTemplate (since PATCH is not supported by default)
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        // Prepare form data for login
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Form data payload (hardcoded credentials, should be replaced with test config)
        String loginPayload = "username=marysmith&password=password123";

        // Make the login request
        HttpEntity<String> request = new HttpEntity<>(loginPayload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/auth/login", request, String.class);

        // Ensure login is successful
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Extract the JSESSIONID from the Set-Cookie header
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader, "Set-Cookie header is missing");

        // Extract JSESSIONID from cookie (might fail if cookie format changes)
        sessionId = setCookieHeader.split(";")[0]; // Example: JSESSIONID=abc123
        assertTrue(sessionId.startsWith("JSESSIONID="), "JSESSIONID is not present in the cookie");
    }

    @Test
    void testUpdateMenuItemAvailability() {
        final Long itemId = 1L;

        // Prepare headers with JSESSIONID for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.COOKIE, sessionId); // Add the session cookie

        // Create the request payload (JSON body)
        HttpEntity<String> request = new HttpEntity<>("{\"isAvailable\": false}", headers);

        // Send the PATCH request to update menu item availability
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/restaurant/menu/items/" + itemId + "/availability",
                HttpMethod.PATCH,
                request,
                String.class
        );

        // Assertions to verify the response is as expected
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().contains("Menu item availability updated successfully"),
                "Response should confirm successful update");
    }
}
