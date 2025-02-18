package com.controller;

import com.dto.LoginRequestDTO;
import com.dto.UserRegistrationDTO;
import com.response.ApiResponse;
import com.response.JwtResponse;
import com.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

/**
 * Handles authentication and user registration.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid UserRegistrationDTO userRegistrationDTO) {
        String message = authService.registerUser(userRegistrationDTO);
        return ResponseEntity.ok(message);
    }

    /**
     * Authenticates a user and returns a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody @Valid LoginRequestDTO request) {
        JwtResponse jwtResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(jwtResponse.getToken()));
    }


}
