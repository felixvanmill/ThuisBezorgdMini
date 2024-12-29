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
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {
        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/dashboard"; // Default fallback URL

        for (final GrantedAuthority authority : authorities) {
            if ("ROLE_CUSTOMER".equals(authority.getAuthority())) {
                redirectUrl = "/customer/home";
                break;
            } else if ("ROLE_RESTAURANT_EMPLOYEE".equals(authority.getAuthority())) {
                redirectUrl = "/restaurant/management";
                break;
            } else if ("ROLE_DELIVERY_PERSON".equals(authority.getAuthority())) {
                redirectUrl = "/delivery/allOrders";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
