package com.service;

import com.model.AppUser;
import com.model.UserRole;
import com.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private AppUserRepository userRepository;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create appUser with the correct role and provide a full name
        appUser = new AppUser("testuser", "password", UserRole.CUSTOMER, "Test User");  // Full name added here
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(appUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());

        // Check for the correct role authority from UserRole enum
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(UserRole.CUSTOMER.getAuthority())));

        // Verify that findByUsername was called once
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(username));

        assertEquals("User not found with username: nonexistentuser", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUsernameIsEmpty() {
        // Arrange
        String username = "";
        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(username));

        assertEquals("User not found with username: ", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(username);
    }
}
