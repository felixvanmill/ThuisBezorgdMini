package com.security;

import com.service.TokenBlacklistService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This filter checks every request for a valid JWT token.
 * It blocks blacklisted tokens and sets authentication for valid ones.
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Constructor to set up token utilities and blacklist service.
     *
     * @param jwtTokenUtil          Used to handle JWT tokens.
     * @param tokenBlacklistService Used to check if tokens are blacklisted.
     */
    public JwtAuthorizationFilter(JwtTokenUtil jwtTokenUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Checks the request for a JWT token, validates it, and sets the user as authenticated if valid.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = getJwtFromRequest(request);

        if (token != null) {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Token is blacklisted.\"}");
                return;
            }

            if (!jwtTokenUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid or expired token.\"}");
                return;
            }

            Authentication auth = jwtTokenUtil.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }


    /**
     * Extracts the token from the Authorization header.
     *
     * @param request The incoming HTTP request.
     * @return The token (without the "Bearer " prefix) or null if not found.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Get the Authorization header
        String bearerToken = request.getHeader("Authorization");
        // Check if it starts with "Bearer " and return the token part
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " from the string
        }
        return null; // No valid token found
    }
}
