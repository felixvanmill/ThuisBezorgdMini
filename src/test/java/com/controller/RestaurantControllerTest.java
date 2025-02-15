//package com.controller;
//
//import com.dto.MenuItemDTO;
//import com.security.JwtTokenUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.core.env.Environment;
//import org.springframework.http.*;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestPropertySource(locations = "classpath:application-test.properties")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class RestaurantControllerTest {
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
//    private JwtTokenUtil jwtTokenUtil; // ✅ Injects JWT utility
//
//    private String baseUrl;
//    private String token;
//
//    @BeforeEach
//    void setup() {
//        baseUrl = "http://localhost:" + port;
//        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//
//        // ✅ Generates a valid JWT token for a restaurant employee
//        token = jwtTokenUtil.generateToken("restaurantEmployee", "ROLE_RESTAURANT_EMPLOYEE");
//    }
//
//    @Test
//    void testGetMenuManagementBySlug() {
//        final String slug = "pizza-place";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(token); // ✅ Uses valid JWT token
//
//        HttpEntity<Void> request = new HttpEntity<>(headers);
//
//        // ✅ Fetch raw response for debugging
//        ResponseEntity<String> rawResponse = restTemplate.exchange(
//                baseUrl + "/restaurant/" + slug + "/menu-management",
//                HttpMethod.GET,
//                request,
//                String.class
//        );
//        System.out.println("RAW RESPONSE: " + rawResponse.getBody());
//
//        // ✅ Fetch and deserialize response as List<MenuItemDTO>
//        ResponseEntity<List<MenuItemDTO>> response = restTemplate.exchange(
//                baseUrl + "/restaurant/" + slug + "/menu-management",
//                HttpMethod.GET,
//                request,
//                new ParameterizedTypeReference<>() {}
//        );
//
//        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected HTTP 200 OK");
//        assertNotNull(response.getBody(), "Response body should not be null");
//        assertFalse(response.getBody().isEmpty(), "Menu should contain at least one item");
//
//        for (MenuItemDTO item : response.getBody()) {
//            assertNotNull(item.getName(), "Menu item name should not be null");
//            assertNotNull(item.getId(), "Menu item ID should not be null");
//        }
//    }
//}
