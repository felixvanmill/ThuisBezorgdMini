package com.service;

import com.exception.ResourceNotFoundException;
import com.exception.ValidationException;
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
     * @throws ValidationException if username or password is invalid.
     */
    public AppUser addUser(AppUser user) {
        validateUser(user); // Validate before saving
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt password
        return userRepository.save(user);
    }

    /**
     * Checks if a username exists.
     *
     * @param username Username to check.
     * @return True if username exists, false otherwise.
     * @throws ResourceNotFoundException if user is not found.
     */
    public boolean userExists(String username) {
        boolean exists = userRepository.findByUsername(username).isPresent();
        if (!exists) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        return true;
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

    /**
     * Validates user details before saving.
     *
     * @param user The user to validate.
     * @throws ValidationException if username or password is invalid.
     */
    private void validateUser(AppUser user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty.");
        }
    }
}
