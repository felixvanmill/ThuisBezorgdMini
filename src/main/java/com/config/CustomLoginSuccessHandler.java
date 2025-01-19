package com.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * Handles successful login events and redirects users based on their role.
 */
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Redirects the user to the appropriate page based on their role.
     *
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the authentication object containing user details
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Get user roles from authentication
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/dashboard"; // Default redirect URL

        // Determine redirect URL based on user role
        for (GrantedAuthority authority : authorities) {
            switch (authority.getAuthority()) {
                case "ROLE_CUSTOMER":
                    redirectUrl = "/customer/home";
                    break;
                case "ROLE_RESTAURANT_EMPLOYEE":
                    redirectUrl = "/restaurant/management";
                    break;
                case "ROLE_DELIVERY_PERSON":
                    redirectUrl = "/delivery/allOrders";
                    break;
                default:
                    break; // Keep default redirect if no roles match
            }
        }

        // Redirect to the determined URL
        response.sendRedirect(redirectUrl);
    }
}
