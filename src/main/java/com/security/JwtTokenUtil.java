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
 * Utility class for handling JWT operations such as generation, validation.
 */
@Component
public class JwtTokenUtil {

    private final String secretKey = "yourSecretKey"; // Should replace this with a secure key in production
    private final long expirationTime = 1000 * 60 * 60; // Token expiration time is set to (1 hour)

    /**
     * This function generates a JWT token for a given username and role.
     *
     * @param username The username of the authenticated user.
     * @param role     The role of the authenticated user.
     * @return A signed JWT token.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", "ROLE_" + role)  // Ensure correct role format
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
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
