package com.service;

import com.model.AppUser;
import com.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Retrieve all users.
     *
     * @return List of all users.
     */
    public List<AppUser> getAllUsers() {
        return this.userRepository.findAll();
    }

    /**
     * Retrieve a user by their ID.
     *
     * @param id The ID of the user.
     * @return An Optional containing the user if found, or empty if not found.
     */
    public Optional<AppUser> getUserById(final Long id) {
        return this.userRepository.findById(id);
    }

    /**
     * Add a new user to the database.
     * Passwords are encrypted before saving.
     *
     * @param user The user to add.
     * @return The saved user.
     */
    public AppUser addUser(final AppUser user) {
        // Encrypt the password before saving the user
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    /**
     * Update an existing user's details.
     *
     * @param id          The ID of the user to update.
     * @param userDetails The new details for the user.
     * @return An Optional containing the updated user if found, or empty if not found.
     */
    public Optional<AppUser> updateUser(final Long id, final AppUser userDetails) {
        return this.userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(userDetails.getUsername());
            // Encrypt the password before updating
            existingUser.setPassword(this.passwordEncoder.encode(userDetails.getPassword()));
            existingUser.setRole(userDetails.getRole());
            existingUser.setFullName(userDetails.getFullName());
            return this.userRepository.save(existingUser);
        });
    }

    /**
     * Delete a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @return True if the user was deleted, false if the user was not found.
     */
    public boolean deleteUser(final Long id) {
        if (this.userRepository.existsById(id)) {
            this.userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Check if a username is already in use.
     *
     * @param username The username to check.
     * @return True if the username exists, false otherwise.
     */
    public boolean userExists(final String username) {
        return this.userRepository.findByUsername(username).isPresent();
    }

    /**
     * Retrieve a user by their username.
     *
     * @param username The username of the user.
     * @return An Optional containing the user if found, or empty if not found.
     */
    public Optional<AppUser> getUserByUsername(final String username) {
        return this.userRepository.findByUsername(username);
    }
}
