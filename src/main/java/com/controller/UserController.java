package com.controller;

import com.model.AppUser;
import com.model.UserRegistrationDTO;
import com.response.JwtResponse;
import com.security.JwtTokenUtil;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

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

    // Get all users
    @GetMapping
    public List<AppUser> getAllUsers() {
        return this.userService.getAllUsers();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public Optional<AppUser> getUserById(@PathVariable final Long id) {
        return this.userService.getUserById(id);
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid final UserRegistrationDTO userRegistrationDTO) {
        if (this.userService.userExists(userRegistrationDTO.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken");
        }

        final AppUser user = new AppUser();
        user.setUsername(userRegistrationDTO.getUsername());
        user.setPassword(this.passwordEncoder.encode(userRegistrationDTO.getPassword())); // Encrypt the password
        user.setRole(userRegistrationDTO.getRole());
        this.userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    // Update an existing user
    @PutMapping("/{id}")
    public AppUser updateUser(@PathVariable final Long id, @RequestBody final AppUser userDetails) {
        return this.userService.updateUser(id, userDetails);
    }

    // Delete a user
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable final Long id) {
        this.userService.deleteUser(id);
    }

    // Login method - Authenticates user and returns a JWT
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody final UserRegistrationDTO authenticationRequest) {
        try {
            // Authenticate the user
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (final AuthenticationException e) {
            // Return a 401 Unauthorized status if authentication fails
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }

        // Fetch the user from the database after authentication
        final AppUser user = this.userService.getUserByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Generate JWT token
        String jwt = this.jwtTokenUtil.generateToken(authenticationRequest.getUsername(), user.getRole());

        // Return the JWT in a response entity
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    // Get user by username (for other purposes like retrieving user info)
    @GetMapping("/username/{username}")
    public ResponseEntity<AppUser> getUserByUsername(@PathVariable final String username) {
        final Optional<AppUser> userOptional = this.userService.getUserByUsername(username);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            throw new RuntimeException("User not found");  // You can handle this exception more gracefully as per your requirements
        }
    }
}
