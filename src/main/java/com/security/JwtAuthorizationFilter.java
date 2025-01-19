package com.security;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A filter to check JWT tokens in every request.
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    // Use JwtTokenUtil to validate tokens
    public JwtAuthorizationFilter(final JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Checks if a JWT token is present and valid.
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws IOException, ServletException {
        final String token = getJwtFromRequest(request);

        // Validate the token if it's there
        if (token != null && jwtTokenUtil.validateToken(token)) {
            // Logic to authenticate the user can go here
        }

        // Continue the filter chain
        chain.doFilter(request, response);
    }

    /**
     * Gets the JWT token from the Authorization header.
     */
    private String getJwtFromRequest(final HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        // Check if the header starts with "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " and return the token
        }
        return null; // No valid token found
    }
}
