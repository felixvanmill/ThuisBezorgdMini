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

@RestController
public class LoginController {

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping("/login")
    public Map<String, Object> getLoginStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("message", "User is not authenticated.");
                return response;
            }

            String username = authentication.getName();
            AppUser user = appUserRepository.findByUsername(username).orElse(null);

            if (user == null) {
                response.put("message", "User not found.");
                return response;
            }

            // Construct detailed response for authenticated users
            response.put("message", "User is authenticated.");
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole().toString());
            response.put("authenticated", true); // Add a flag for easier client-side checks

        } catch (Exception e) {
            response.put("error", "An error occurred while checking login status.");
            response.put("details", e.getMessage());
        }

        return response;
    }
}
