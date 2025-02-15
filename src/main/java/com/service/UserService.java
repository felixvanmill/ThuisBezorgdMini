// src/main/java/com/service/UserService.java

package com.service;

import com.model.AppUser;
import com.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * Retrieve a user by their ID.
     *
     * @param id User ID.
     * @return User if found, or empty Optional.
     */
    public Optional<AppUser> getUserById(Long id) {
        return userRepository.findById(id);
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
     * @return User if found, or empty Optional.
     */
    public Optional<AppUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
