package com.service;

import com.model.AppUser;
import com.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;  // Import PasswordEncoder, not BCryptPasswordEncoder
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Use PasswordEncoder interface

    public List<AppUser> getAllUsers() {
        return this.userRepository.findAll();
    }

    public Optional<AppUser> getUserById(final Long id) {
        return this.userRepository.findById(id);
    }

    public AppUser addUser(final AppUser user) {
        // Encrypt the password before saving the user
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    public AppUser updateUser(final Long id, final AppUser userDetails) {
        final Optional<AppUser> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            final AppUser user = userOptional.get();
            user.setUsername(userDetails.getUsername());
            // Encrypt the password before updating
            user.setPassword(this.passwordEncoder.encode(userDetails.getPassword()));
            user.setRole(userDetails.getRole());
            return this.userRepository.save(user);
        }
        return null;
    }

    public void deleteUser(final Long id) {
        this.userRepository.deleteById(id);
    }

    public boolean userExists(final String username) {
        return this.userRepository.findByUsername(username).isPresent();
    }

    public Optional<AppUser> getUserByUsername(final String username) {
        return this.userRepository.findByUsername(username);
    }
}
