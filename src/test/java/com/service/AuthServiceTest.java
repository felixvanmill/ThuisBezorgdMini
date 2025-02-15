//package com.service;
//
//import com.dto.LoginRequestDTO;
//import com.dto.UserRegistrationDTO;
//import com.model.AppUser;
//import com.model.UserRole;
//import com.response.JwtResponse;
//import com.security.JwtTokenUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class AuthServiceTest {
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private JwtTokenUtil jwtTokenUtil;
//
//    @InjectMocks
//    private AuthService authService;
//
//    private UserRegistrationDTO registrationDTO;
//    private LoginRequestDTO loginRequest;
//    private AppUser user;
//
//    @BeforeEach
//    void setUp() {
//        registrationDTO = new UserRegistrationDTO();
//        registrationDTO.setUsername("testuser");
//        registrationDTO.setPassword("password123");
//        registrationDTO.setRole("CUSTOMER"); // ✅ Ensure it matches the enum values
//        registrationDTO.setFullName("Test User");
//
//        loginRequest = new LoginRequestDTO();
//        loginRequest.setUsername("testuser");
//        loginRequest.setPassword("password123");
//
//        user = new AppUser("testuser", "encodedPassword", UserRole.CUSTOMER, "Test User");
//    }
//
//    @Test
//    void testRegisterUser_Success() {
//        when(userService.userExists("testuser")).thenReturn(false);
//        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
//
//        ResponseEntity<?> response = authService.registerUser(registrationDTO);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals("User registered successfully.", response.getBody());
//        verify(userService).addUser(any(AppUser.class));
//    }
//
//    @Test
//    void testRegisterUser_Fails_WhenUsernameTaken() {
//        when(userService.userExists("testuser")).thenReturn(true);
//
//        ResponseEntity<?> response = authService.registerUser(registrationDTO);
//
//        assertEquals(400, response.getStatusCodeValue());
//        assertEquals("Username is already taken.", response.getBody());
//        verify(userService, never()).addUser(any());
//    }
//
//    @Test
//    void testLogin_Success() {
//        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
//
//        // ✅ Fix: Use `getAuthority()` in test to match AuthService
//        when(jwtTokenUtil.generateToken("testuser", UserRole.CUSTOMER.getAuthority()))
//                .thenReturn("mockedToken");
//
//        ResponseEntity<JwtResponse> response = authService.login(loginRequest);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals("mockedToken", response.getBody().getToken());
//    }
//
//    @Test
//    void testLogin_Fails_WhenUserNotFound() {
//        when(userService.getUserByUsername("testuser")).thenReturn(Optional.empty());
//
//        ResponseEntity<JwtResponse> response = authService.login(loginRequest);
//
//        assertEquals(404, response.getStatusCodeValue());
//        assertEquals("User not found.", response.getBody().getToken());
//    }
//
//    @Test
//    void testLogin_Fails_WhenPasswordIncorrect() {
//        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);
//
//        ResponseEntity<JwtResponse> response = authService.login(loginRequest);
//
//        assertEquals(401, response.getStatusCodeValue());
//        assertEquals("Incorrect username or password.", response.getBody().getToken());
//    }
//}
