package com.controller;

import com.dto.LoginRequestDTO;
import com.dto.UserRegistrationDTO;
import com.model.AppUser;
import com.model.UserRole;
import com.response.JwtResponse;
import com.security.JwtTokenUtil;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

/**
 * Handles authentication and user registration.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationDTO userRegistrationDTO) {
        if (userService.userExists(userRegistrationDTO.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken.");
        }

        UserRole role = UserRole.valueOf(userRegistrationDTO.getRole().toUpperCase());
        AppUser user = new AppUser(
                userRegistrationDTO.getUsername(),
                passwordEncoder.encode(userRegistrationDTO.getPassword()),
                role,
                userRegistrationDTO.getFullName()
        );

        userService.addUser(user);
        return ResponseEntity.ok("User registered successfully.");
    }

    /**
     * Authenticates a user and returns a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO request) {
        Optional<AppUser> userOptional = userService.getUserByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found.");
        }

        AppUser user = userOptional.get();

        // 🔥 Compare plaintext password with hashed password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Incorrect username or password.");
        }

        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
