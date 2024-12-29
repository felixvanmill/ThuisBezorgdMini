package com.security;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    // Constructor accepts JwtTokenUtil
    public JwtAuthorizationFilter(final JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final String token = this.getJwtFromRequest(request);
        if (null != token && this.jwtTokenUtil.validateToken(token)) {
            // You can add authentication logic here (extract user details, etc.)
        }
        chain.doFilter(request, response);
    }

    private String getJwtFromRequest(final HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        if (null != bearerToken && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Extract token from "Bearer <token>"
        }
        return null;
    }
}
