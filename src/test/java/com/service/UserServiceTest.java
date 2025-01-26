package com.service;

import com.model.AppUser;
import com.model.UserRole;
import com.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes @InjectMocks and @Mock annotations
    }

    @Test
    void testAddUser() {
        // Mock user
        AppUser user = new AppUser("testuser", "password123", UserRole.CUSTOMER, "Test User");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(AppUser.class))).thenReturn(user);

        AppUser savedUser = userService.addUser(user);

        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void testGetUserById() {
        // Mock user
        AppUser mockUser = new AppUser("testuser", "encodedPassword", UserRole.CUSTOMER, "Test User");
        mockUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Optional<AppUser> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testUserExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new AppUser()));

        // Act
        boolean exists = userService.userExists("testuser");

        // Assert
        assertTrue(exists);
        verify(userRepository, times(1)).findByUsername("testuser");
    }


    @Test
    void testGetUserByUsername() {
        // Mock user
        AppUser mockUser = new AppUser("testuser", "encodedPassword", UserRole.CUSTOMER, "Test User");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        Optional<AppUser> result = userService.getUserByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }
}
