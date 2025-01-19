package com.controller;

import com.dto.UserRegistrationDTO;
import com.model.AppUser;
import com.model.UserRole;
import com.response.JwtResponse;
import com.security.JwtTokenUtil;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * Controller for managing users: registration, authentication, and CRUD operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Fetch all users.
     *
     * @return List of all registered users.
     */
    @GetMapping
    public List<AppUser> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Fetch a user by their ID.
     *
     * @param id User ID.
     * @return User details or error message.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<AppUser> userOptional = userService.getUserById(id);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    /**
     * Register a new user.
     *
     * @param userRegistrationDTO User registration data.
     * @return Success or error response.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationDTO userRegistrationDTO) {
        if (userService.userExists(userRegistrationDTO.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username is already taken"));
        }

        try {
            UserRole role = UserRole.valueOf(userRegistrationDTO.getRole().toUpperCase());
            AppUser user = new AppUser(
                    userRegistrationDTO.getUsername(),
                    passwordEncoder.encode(userRegistrationDTO.getPassword()),
                    role,
                    userRegistrationDTO.getFullName()
            );
            userService.addUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role provided. Allowed roles: CUSTOMER, RESTAURANT_EMPLOYEE, DELIVERY_PERSON."));
        }
    }

    /**
     * Update an existing user by their ID.
     *
     * @param id          User ID.
     * @param userDetails Updated user details.
     * @return Success or error response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody AppUser userDetails) {
        Optional<AppUser> updatedUser = userService.updateUser(id, userDetails);
        if (updatedUser.isPresent()) {
            return ResponseEntity.ok(updatedUser.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    /**
     * Delete a user by their ID.
     *
     * @param id User ID.
     * @return Success or error response.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param authenticationRequest User credentials.
     * @return JWT token or error response.
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid UserRegistrationDTO authenticationRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Incorrect username or password"));
        }

        Optional<AppUser> userOptional = userService.getUserByUsername(authenticationRequest.getUsername());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        AppUser user = userOptional.get();
        String jwt = jwtTokenUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    /**
     * Fetch a user by their username.
     *
     * @param username Username.
     * @return User details or an error message.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<AppUser> userOptional = userService.getUserByUsername(username);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }
}
