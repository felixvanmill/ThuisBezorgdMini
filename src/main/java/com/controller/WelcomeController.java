package com.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WelcomeController {

    @GetMapping("/greeting")
    @ResponseBody
    public String greeting() {
        // Retrieve the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get the user's role
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        return "Hi, " + username + "! How are you today? Your current rights are those of " + role + ".";
    }
}
