package com.service;

import com.model.AppUser;
import com.model.UserRole;
import com.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private AppUser userWithoutRolePrefix;
    private AppUser userWithPrefixedRole;

    @BeforeEach
    void setUp() {
        // User with a role that does not have "ROLE_" prefix
        userWithoutRolePrefix = new AppUser();
        userWithoutRolePrefix.setUsername("testuser");
        userWithoutRolePrefix.setPassword("encodedPassword");
        userWithoutRolePrefix.setRole(UserRole.CUSTOMER); // "CUSTOMER" (without "ROLE_")

        // User with a correctly prefixed role
        userWithPrefixedRole = new AppUser();
        userWithPrefixedRole.setUsername("employeeuser");
        userWithPrefixedRole.setPassword("encodedPassword");
        userWithPrefixedRole.setRole(UserRole.RESTAURANT_EMPLOYEE); // "RESTAURANT_EMPLOYEE" -> Should become "ROLE_RESTAURANT_EMPLOYEE"
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userWithoutRolePrefix));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("ROLE_CUSTOMER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("unknownUser")
        );

        assertEquals("User not found with username: unknownUser", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserWithPrefixedRole() {
        when(userRepository.findByUsername("employeeuser")).thenReturn(Optional.of(userWithPrefixedRole));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("employeeuser");

        assertNotNull(userDetails);
        assertEquals("employeeuser", userDetails.getUsername());
        assertEquals("ROLE_RESTAURANT_EMPLOYEE", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void testLoadUserByUsername_NullUsername_ShouldThrowException() {
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(null)
        );
        assertEquals("User not found with username: null", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_EmptyUsername_ShouldThrowException() {
        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("")
        );
        assertEquals("User not found with username: ", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_LoggingWhenUserNotFound() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("unknownUser")
        );

        assertEquals("User not found with username: unknownUser", exception.getMessage());
        verify(userRepository).findByUsername("unknownUser");
    }

    @Test
    void testLoadUserByUsername_MultipleRoles() {
        for (UserRole role : UserRole.values()) {
            AppUser testUser = new AppUser();
            testUser.setUsername("testUser" + role.name());
            testUser.setPassword("encodedPassword");
            testUser.setRole(role);

            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(testUser.getUsername());

            assertNotNull(userDetails);
            assertEquals(testUser.getUsername(), userDetails.getUsername());
            assertEquals("ROLE_" + role.name(), userDetails.getAuthorities().iterator().next().getAuthority());
        }
    }

}
