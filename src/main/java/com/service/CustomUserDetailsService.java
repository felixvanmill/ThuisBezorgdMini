// src/main/java/com/service/CustomUserDetailsService.java

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
 * Service to load user-specific data for authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository userRepository;

    /**
     * Loads user details by username for authentication.
     *
     * @param username The username to look up.
     * @return UserDetails containing the user's authentication data.
     * @throws UsernameNotFoundException If the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user in the database
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Assign the user's role as a GrantedAuthority
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        // Create and return a Spring Security User object
        return new User(
                user.getUsername(),      // Username
                user.getPassword(),      // Encrypted password
                true,                    // Account is enabled
                true,                    // Account is not expired
                true,                    // Credentials are not expired
                true,                    // Account is not locked
                Collections.singleton(authority) // User's role as a single authority
        );
    }
}
