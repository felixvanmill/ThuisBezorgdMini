package com.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;

/**
 * Utility class for handling JWT operations like generation, validation, and extracting info.
 */
@Component
public class JwtTokenUtil {

    private final String secretKey = "yourSecretKey"; // Replace with a secure key
    private final long expirationTime = 1000 * 60 * 60; // Token expires in 1 hour

    /**
     * Extracts the username from the JWT.
     */
    public String getUsernameFromToken(final String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Extracts the role from the JWT.
     */
    public String getRoleFromToken(final String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    /**
     * Gets all claims from the JWT.
     */
    private Claims getClaimsFromToken(final String token) {
        return Jwts.parser()
                .setSigningKey(secretKey) // Use the secret key
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Validates the token by checking if it's expired.
     */
    public boolean validateToken(final String token) {
        return !isTokenExpired(token);
    }

    /**
     * Checks if the token has expired.
     */
    private boolean isTokenExpired(final String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }

    /**
     * Creates an authentication object based on the JWT.
     */
    public Authentication getAuthentication(final String token) {
        String username = getUsernameFromToken(token);
        String role = getRoleFromToken(token);
        User principal = new User(username, "", new ArrayList<>()); // No authorities for now
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    /**
     * Generates a JWT for the given username and role.
     */
    public String generateToken(final String username, final String role) {
        return Jwts.builder()
                .setSubject(username) // Set username as the subject
                .claim("role", role)  // Add role as a claim
                .setIssuedAt(new Date()) // Set token issue time
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Set expiry time
                .signWith(SignatureAlgorithm.HS256, secretKey) // Sign using the secret key
                .compact(); // Build the token
    }
}
