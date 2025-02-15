//package com.service;
//
//import com.model.AppUser;
//import com.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CustomUserDetailsServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private CustomUserDetailsService customUserDetailsService;
//
//    private AppUser user;
//
//    @BeforeEach
//    void setUp() {
//        user = new AppUser();
//        user.setUsername("testuser");
//        user.setPassword("encodedPassword");
//    }
//
//    @Test
//    void testLoadUserByUsername_Success() {
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//
//        var userDetails = customUserDetailsService.loadUserByUsername("testuser");
//
//        assertNotNull(userDetails);
//        assertEquals("testuser", userDetails.getUsername());
//    }
//
//    @Test
//    void testLoadUserByUsername_UserNotFound() {
//        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());
//
//        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
//                customUserDetailsService.loadUserByUsername("unknownUser")
//        );
//
//        assertEquals("User not found: unknownUser", exception.getMessage());
//    }
//}
