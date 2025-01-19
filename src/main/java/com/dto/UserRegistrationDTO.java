package com.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user registration details.
 */
public class UserRegistrationDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String role;

    @NotBlank
    private String fullName;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
