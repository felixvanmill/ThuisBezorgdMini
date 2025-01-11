package com.controller;

import com.model.AppUser;
import com.model.UserRole;
import com.dto.UserRegistrationDTO;
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
     * Get all users.
     *
     * @return List of all users.
     */
    @GetMapping
    public List<AppUser> getAllUsers() {
        return this.userService.getAllUsers();
    }

    /**
     * Get user by ID.
     *
     * @param id The user's ID.
     * @return User details or error message.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable final Long id) {
        Optional<AppUser> userOptional = this.userService.getUserById(id);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * Register a new user.
     *
     * @param userRegistrationDTO User registration data.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid final UserRegistrationDTO userRegistrationDTO) {
        if (this.userService.userExists(userRegistrationDTO.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken");
        }

        UserRole role;
        try {
            // Convert string to UserRole enum
            role = UserRole.valueOf(userRegistrationDTO.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid role provided. Allowed roles: CUSTOMER, RESTAURANT_EMPLOYEE, DELIVERY_PERSON.");
        }

        // Create new user
        final AppUser user = new AppUser(
                userRegistrationDTO.getUsername(),
                this.passwordEncoder.encode(userRegistrationDTO.getPassword()), // Encrypt password
                role,
                userRegistrationDTO.getFullName() // Access fullName properly
        );

        this.userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    /**
     * Update an existing user.
     *
     * @param id          User ID to update.
     * @param userDetails Updated user details.
     * @return ResponseEntity indicating success or failure.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable final Long id, @RequestBody final AppUser userDetails) {
        Optional<AppUser> updatedUser = this.userService.updateUser(id, userDetails);

        if (updatedUser.isPresent()) {
            return ResponseEntity.ok(updatedUser.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * Delete a user.
     *
     * @param id The user's ID to delete.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable final Long id) {
        boolean deleted = this.userService.deleteUser(id);

        if (deleted) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    /**
     * Authenticate user and return a JWT token.
     *
     * @param authenticationRequest User credentials.
     * @return ResponseEntity with JWT token or error message.
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid final UserRegistrationDTO authenticationRequest) {
        try {
            // Authenticate the user
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );
        } catch (final AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }

        // Fetch user after authentication
        final Optional<AppUser> userOptional = this.userService.getUserByUsername(authenticationRequest.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        final AppUser user = userOptional.get(); // Get the user from Optional

        // Generate JWT
        String jwt = this.jwtTokenUtil.generateToken(user.getUsername(), user.getRole().name());

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    /**
     * Get user by username.
     *
     * @param username The username to search for.
     * @return ResponseEntity containing user details.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable final String username) {
        final Optional<AppUser> userOptional = this.userService.getUserByUsername(username);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}
