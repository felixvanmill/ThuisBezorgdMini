package com.service;

import com.model.AppUser;
import com.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service class to load user-specific data during authentication.
 * Integrates Spring Security with the AppUser entity.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository userRepository;

    /**
     * Loads the user by username for authentication purposes.
     * Converts AppUser into Spring Security's UserDetails.
     *
     * @param username The username to search for.
     * @return A UserDetails object containing authentication information.
     * @throws UsernameNotFoundException if the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        // Fetch the user from the database
        final AppUser user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Map the user's role to a GrantedAuthority
        final GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // Return a Spring Security User object
        return new User(
                user.getUsername(), // Username
                user.getPassword(), // Encrypted password
                true, // Account is enabled
                true, // Account is not expired
                true, // Credentials are not expired
                true, // Account is not locked
                Collections.singleton(authority) // Single authority based on the user's role
        );
    }
}
