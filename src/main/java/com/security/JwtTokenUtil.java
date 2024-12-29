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

    private final String secretKey = "yourSecretKey";  // Use a more secure key in production
    private final long expirationTime = 1000 * 60 * 60;  // 1 hour expiration

    // Method to extract username from JWT
    public String getUsernameFromToken(final String token) {
        return this.getClaimsFromToken(token).getSubject();
    }

    // Method to extract role from JWT
    public String getRoleFromToken(final String token) {
        return this.getClaimsFromToken(token).get("role", String.class);
    }

    // Method to get claims from the JWT
    private Claims getClaimsFromToken(final String token) {
        return Jwts.parser()
                .setSigningKey(this.secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // Method to validate the token
    public boolean validateToken(final String token) {
        return !this.isTokenExpired(token);
    }

    // Method to check if the token is expired
    private boolean isTokenExpired(final String token) {
        return this.getClaimsFromToken(token).getExpiration().before(new Date());
    }

    // Create Authentication based on the JWT
    public Authentication getAuthentication(final String token) {
        final String username = this.getUsernameFromToken(token);
        final String role = this.getRoleFromToken(token); // Get the role from token
        final User principal = new User(username, "", new ArrayList<>());  // Empty authorities for now
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    // Method to generate the JWT
    public String generateToken(final String username, final String role) {
        return Jwts.builder()
                .setSubject(username)  // Set the subject (usually username)
                .claim("role", role)   // Add the role as a claim
                .setIssuedAt(new Date())  // Set the issued time
                .setExpiration(new Date(System.currentTimeMillis() + this.expirationTime))  // Set expiration time
                .signWith(SignatureAlgorithm.HS256, this.secretKey)  // Sign the token with the secret key
                .compact();
    }
}
