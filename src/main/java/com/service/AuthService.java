package com.service;

import com.dto.LoginRequestDTO;
import com.dto.UserRegistrationDTO;
import com.exception.AuthenticationException;
import com.exception.ResourceNotFoundException;
import com.exception.ValidationException;
import com.model.AppUser;
import com.model.UserRole;
import com.response.JwtResponse;
import com.security.JwtTokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Registers a new user.
     *
     * @param userRegistrationDTO Registration details.
     * @throws ValidationException if username is already taken.
     */
    public String registerUser(UserRegistrationDTO userRegistrationDTO) {
        if (userService.userExists(userRegistrationDTO.getUsername())) {
            throw new ValidationException("Username is already taken.");
        }

        UserRole role = UserRole.valueOf(userRegistrationDTO.getRole().toUpperCase());
        AppUser user = new AppUser(
                userRegistrationDTO.getUsername(),
                passwordEncoder.encode(userRegistrationDTO.getPassword()),
                role,
                userRegistrationDTO.getFullName()
        );

        userService.addUser(user);
        return "User registered successfully.";
    }


    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param request Login request DTO.
     * @return JWT token response.
     * @throws ResourceNotFoundException if the user is not found.
     * @throws AuthenticationException if the password is incorrect.
     */
    public JwtResponse login(LoginRequestDTO request) {
        AppUser user = userService.getUserByUsername(request.getUsername());


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Incorrect username or password.");
        }

        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole().name());
        return new JwtResponse(token);
    }
}
