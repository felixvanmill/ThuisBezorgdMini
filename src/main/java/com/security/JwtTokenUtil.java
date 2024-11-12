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

@Component
public class JwtTokenUtil {

    private String secretKey = "yourSecretKey";  // Use a more secure key in production
    private long expirationTime = 1000 * 60 * 60;  // 1 hour expiration

    // Method to extract username from JWT
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // Method to extract role from JWT
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    // Method to get claims from the JWT
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // Method to validate the token
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    // Method to check if the token is expired
    private boolean isTokenExpired(String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }

    // Create Authentication based on the JWT
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        String role = getRoleFromToken(token); // Get the role from token
        User principal = new User(username, "", new ArrayList<>());  // Empty authorities for now
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    // Method to generate the JWT
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)  // Set the subject (usually username)
                .claim("role", role)   // Add the role as a claim
                .setIssuedAt(new Date())  // Set the issued time
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))  // Set expiration time
                .signWith(SignatureAlgorithm.HS256, secretKey)  // Sign the token with the secret key
                .compact();
    }
}
