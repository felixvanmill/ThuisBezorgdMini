package com.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // Role-based redirection after login
        switch (role) {
            case "ROLE_CUSTOMER":
                return "redirect:/customer/home";  // Customer dashboard
            case "ROLE_RESTAURANT_EMPLOYEE":
                return "redirect:/restaurant/home";  // Restaurant employee dashboard
            case "ROLE_DELIVERY_PERSON":
                return "redirect:/delivery/home";  // Delivery person dashboard
            default:
                return "redirect:/error";
        }
    }
}
