package com.service;

import com.model.AppUser;
import com.repository.AppUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service for loading user details during authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public CustomUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user details by username.
     *
     * @param username The username to search for.
     * @return A UserDetails object with user info.
     * @throws UsernameNotFoundException If the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user in the database
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("User not found with username: " + username); // Logging
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        // Ensure the role is prefixed correctly
        String roleName = user.getRole().name();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        // Create authority object
        GrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        // Return user details for Spring Security
        return new User(
                user.getUsername(),
                user.getPassword(),
                true,  // Account is enabled
                true,  // Account is not expired
                true,  // Credentials are not expired
                true,  // Account is not locked
                Collections.singleton(authority) // User's role
        );
    }
}
