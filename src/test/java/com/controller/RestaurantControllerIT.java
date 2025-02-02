package com.controller;

import com.dto.MenuItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestaurantControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private String sessionId;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;

        // Prepare form data for login
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Hardcoded credentials (should be replaced with configurable test data)
        String loginPayload = "username=marysmith&password=password123";

        // Make the login request
        HttpEntity<String> request = new HttpEntity<>(loginPayload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/auth/login", request, String.class);

        // Ensure login is successful
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Login request failed");

        // Extract the JSESSIONID from the Set-Cookie header
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader, "Set-Cookie header is missing");

        // Simple extraction of JSESSIONID; might fail if cookie format changes
        sessionId = setCookieHeader.split(";")[0]; // Example: JSESSIONID=abc123
        assertTrue(sessionId.startsWith("JSESSIONID="), "JSESSIONID is not present in the cookie");
    }

    @Test
    void testGetMenuManagementBySlug() {
        final String slug = "pizza-place";

        // Prepare headers with JSESSIONID for authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, sessionId); // Add session cookie

        // Create the HTTP entity with headers
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Send the GET request to retrieve menu items
        ResponseEntity<List<MenuItemDTO>> response = restTemplate.exchange(
                baseUrl + "/restaurant/" + slug + "/menu-management",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {} // Correctly handles List<MenuItemDTO>
        );

        // Ensure the response is successful
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertFalse(response.getBody().isEmpty(), "Menu should contain at least one item");

        // Verify each menu item has necessary fields populated
        for (MenuItemDTO item : response.getBody()) {
            assertNotNull(item.getName(), "Menu item name should not be null");
            assertNotNull(item.getId(), "Menu item ID should not be null");
        }
    }
}
