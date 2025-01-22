package com.config;

import com.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles login success and creates a JWT token for the user.
 */
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Injects the JwtTokenUtil for generating JWT tokens.
     *
     * @param jwtTokenUtil Used to create and manage JWT tokens.
     */
    @Autowired
    public CustomLoginSuccessHandler(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Called when a user successfully logs in.
     *
     * @param request        The HTTP request.
     * @param response       The HTTP response.
     * @param authentication Contains details about the logged-in user.
     * @throws IOException      If something goes wrong with writing the response.
     * @throws ServletException If there's an issue processing the request.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // Get the user's username and role from the authentication object
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // Create a JWT token for the user
        String token = jwtTokenUtil.generateToken(username, role);

        // Send the token in the HTTP response as JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"token\": \"%s\"}", token));
    }
}
