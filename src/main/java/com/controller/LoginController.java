package com.controller;

import com.model.AppUser;
import com.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles login status checks and user information retrieval.
 */
@RestController
public class LoginController {

    @Autowired
    private AppUserRepository appUserRepository;

    /**
     * Retrieves the login status of the currently authenticated user.
     *
     * @return A map containing login status and user details.
     */
    @GetMapping("/login")
    public Map<String, Object> getLoginStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get the current authentication object
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Check if the user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("message", "User is not authenticated.");
                response.put("authenticated", false);
                return response;
            }

            // Retrieve the authenticated user's username
            String username = authentication.getName();
            AppUser user = appUserRepository.findByUsername(username).orElse(null);

            // Handle cases where the user is not found in the database
            if (user == null) {
                response.put("message", "User not found.");
                response.put("authenticated", false);
                return response;
            }

            // Construct response for authenticated users
            response.put("message", "User is authenticated.");
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole().toString());
            response.put("authenticated", true); // Flag for client-side checks

        } catch (Exception e) {
            // Handle unexpected errors
            response.put("error", "An error occurred while checking login status.");
            response.put("details", e.getMessage());
        }

        return response;
    }
}
