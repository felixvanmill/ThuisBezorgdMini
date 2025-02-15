package com.service;

import com.dto.LoginRequestDTO;
import com.dto.UserRegistrationDTO;
import com.model.AppUser;
import com.model.UserRole;
import com.response.JwtResponse;
import com.security.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Registers a new user.
     */
    public ResponseEntity<?> registerUser(UserRegistrationDTO userRegistrationDTO) {
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
     * Authenticates a user and generates a JWT token.
     */
    public ResponseEntity<JwtResponse> login(LoginRequestDTO request) {
        Optional<AppUser> userOptional = userService.getUserByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(new JwtResponse("User not found."));
        }

        AppUser user = userOptional.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(new JwtResponse("Incorrect username or password."));
        }

        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
