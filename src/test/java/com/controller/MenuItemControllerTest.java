//package com.controller;
//
//import com.security.JwtTokenUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.core.env.Environment;
//import org.springframework.http.*;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestPropertySource(locations = "classpath:application-test.properties")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class MenuItemControllerTest {
//
//    @LocalServerPort
//    private int port;
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private Environment env;
//
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;
//
//    private String baseUrl;
//    private String token;
//
//    @BeforeEach
//    void setup() {
//        baseUrl = "http://localhost:" + port;
//        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//
//        // Generate a valid JWT token for a restaurant employee
//        token = jwtTokenUtil.generateToken("restaurantEmployee", "ROLE_RESTAURANT_EMPLOYEE");
//    }
//
//    @Test
//    void testUpdateMenuItemAvailability() {
//        final Long itemId = 1L;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(token); // Use valid JWT
//
//        HttpEntity<String> request = new HttpEntity<>("{\"isAvailable\": false}", headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                baseUrl + "/restaurant/menu/items/" + itemId + "/availability",
//                HttpMethod.PATCH,
//                request,
//                String.class
//        );
//
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
//        assertNotNull(response.getBody(), "Response body should not be null");
//        assertTrue(response.getBody().contains("Menu item availability updated successfully"),
//                "Response should confirm successful update");
//    }
//}