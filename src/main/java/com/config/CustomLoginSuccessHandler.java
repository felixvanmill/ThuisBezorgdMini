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

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/dashboard"; // Default fallback URL

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_CUSTOMER")) {
                redirectUrl = "/customer/home";
                break;
            } else if (authority.getAuthority().equals("ROLE_RESTAURANT_EMPLOYEE")) {
                redirectUrl = "/restaurant/management";
                break;
            } else if (authority.getAuthority().equals("ROLE_DELIVERY_PERSON")) {
                redirectUrl = "/delivery/allOrders";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
