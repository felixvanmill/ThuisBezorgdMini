package com.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for user registration details.
 */
public class UserRegistrationDTO {

    @NotBlank(message = "Gebruikersnaam is verplicht")
    @Size(min = 3, max = 50, message = "Gebruikersnaam moet tussen 3 en 50 tekens zijn")
    private String username;

    @NotBlank(message = "Wachtwoord is verplicht")
    @Size(min = 8, max = 100, message = "Wachtwoord moet minimaal 8 tekens lang zijn")
    private String password;

    @NotBlank(message = "Rol is verplicht")
    private String role;

    @NotBlank(message = "Volledige naam is verplicht")
    @Size(min = 2, max = 100, message = "Volledige naam moet tussen 2 en 100 tekens zijn")
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
