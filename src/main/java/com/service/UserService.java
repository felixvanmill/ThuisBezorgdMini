package com.service;

import com.exception.ResourceNotFoundException;
import com.model.AppUser;
import com.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for handling user-related operations.
 */
@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor-based Dependency Injection.
     */
    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieve a user by their ID.
     *
     * @param id User ID.
     * @return User if found.
     * @throws ResourceNotFoundException if user is not found.
     */
    public AppUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    /**
     * Add a new user to the database with encrypted password.
     *
     * @param user User to add.
     * @return Saved user.
     */
    public AppUser addUser(AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt password
        return userRepository.save(user);
    }

    /**
     * Check if a username exists.
     *
     * @param username Username to check.
     * @return True if username exists, false otherwise.
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Retrieve a user by their username.
     *
     * @param username Username to find.
     * @return User if found.
     * @throws ResourceNotFoundException if user is not found.
     */
    public AppUser getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
}
