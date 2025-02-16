package com.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for login requests (only username and password).
 */
public class LoginRequestDTO {

    @NotBlank(message = "Gebruikersnaam is verplicht")
    @Size(min = 3, max = 50, message = "Gebruikersnaam moet tussen 3 en 50 tekens zijn")
    private String username;

    @NotBlank(message = "Wachtwoord is verplicht")
    @Size(min = 6, max = 100, message = "Wachtwoord moet minimaal 6 tekens lang zijn")
    private String password;

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
}
