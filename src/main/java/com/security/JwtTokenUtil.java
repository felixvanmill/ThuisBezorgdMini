package com.security;

import io.jsonwebtoken.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;

/**
 * Utility class for handling JWT operations such as generation, validation, and extraction of claims.
 */
@Component
public class JwtTokenUtil {

    private final String secretKey = "yourSecretKey"; // Replace with a secure key in production
    private final long expirationTime = 1000 * 60 * 60; // Token expiration time (1 hour)

    /**
     * Generates a JWT token for a given username and role.
     *
     * @param username The username of the authenticated user.
     * @param role     The role of the authenticated user.
     * @return A signed JWT token.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username) // Set the username as the token subject
                .claim("role", role)  // Add the user's role as a claim
                .setIssuedAt(new Date()) // Set the issue date
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Set the expiration date
                .signWith(SignatureAlgorithm.HS256, secretKey) // Sign the token with the secret key
                .compact();
    }

    /**
     * Validates the given JWT token.
     *
     * @param token The JWT token to validate.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token); // Parse and validate the token
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            System.out.println("Invalid token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extracts claims from a JWT token.
     *
     * @param token The JWT token to parse.
     * @return The claims contained in the token.
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey) // Set the secret key for validation
                .parseClaimsJws(token)    // Parse the token
                .getBody();
    }

    /**
     * Retrieves an Authentication object from the JWT token.
     *
     * @param token The JWT token.
     * @return An Authentication object containing the user's details and authorities.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsFromToken(token);
        String username = claims.getSubject(); // Extract the username
        String role = claims.get("role", String.class); // Extract the role

        // Create a Spring Security User with the extracted information
        User principal = new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role)));

        // Return an authentication token with the user's authorities
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }
}
