package com.service;

import com.dto.LoginRequestDTO;
import com.dto.UserRegistrationDTO;
import com.exception.AuthenticationException;
import com.exception.ResourceNotFoundException;
import com.model.AppUser;
import com.model.UserRole;
import com.response.JwtResponse;
import com.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationDTO registrationDTO;
    private LoginRequestDTO loginRequest;
    private AppUser user;

    @BeforeEach
    void setUp() {
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setPassword("password123");
        registrationDTO.setRole("CUSTOMER"); // ✅ Ensure it matches the enum values
        registrationDTO.setFullName("Test User");

        loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        user = new AppUser("testuser", "encodedPassword", UserRole.CUSTOMER, "Test User");
    }

    @Test
    void testRegisterUser_Success() {
        when(userService.userExists("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        String response = authService.registerUser(registrationDTO);

        assertEquals("User registered successfully.", response);
        verify(userService).addUser(any(AppUser.class));
    }

    @Test
    void testRegisterUser_Fails_WhenUsernameTaken() {
        when(userService.userExists("testuser")).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> authService.registerUser(registrationDTO));

        assertEquals("Username is already taken.", exception.getMessage());
        verify(userService, never()).addUser(any());
    }

    @Test
    void testLogin_Success() {
        when(userService.getUserByUsername("testuser")).thenReturn(user); // FIXED ✅
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        when(jwtTokenUtil.generateToken("testuser", UserRole.CUSTOMER.name()))
                .thenReturn("mockedToken");

        JwtResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockedToken", response.getToken());
    }

    @Test
    void testLogin_Fails_WhenUserNotFound() {
        when(userService.getUserByUsername("testuser")).thenThrow(new ResourceNotFoundException("User not found."));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));

        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void testLogin_Fails_WhenPasswordIncorrect() {
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        Exception exception = assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));

        assertEquals("Incorrect username or password.", exception.getMessage());
    }
}
