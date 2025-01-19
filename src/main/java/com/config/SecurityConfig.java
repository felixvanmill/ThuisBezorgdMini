package com.config;

import com.service.CustomUserDetailsService;
import com.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * Configures security settings for the application, including authentication, authorization, and logout handling.
 */
@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Constructor for injecting required services.
     *
     * @param userDetailsService   Service for loading user details.
     * @param tokenBlacklistService Service for managing token blacklisting.
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Defines the security filter chain, including endpoint protection, login, and logout configurations.
     *
     * @param http The HttpSecurity object for configuring security settings.
     * @return A configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for simplicity (not recommended for production APIs)
                .csrf(csrf -> csrf.disable())

                // Define access rules for different endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/error").permitAll() // Public endpoints
                        .requestMatchers("/customer/**").hasRole("CUSTOMER") // Customer-specific endpoints
                        .requestMatchers("/restaurant/**").hasRole("RESTAURANT_EMPLOYEE") // Restaurant employee endpoints
                        .requestMatchers("/orders/customer/**").hasRole("CUSTOMER") // Customer order endpoints
                        .requestMatchers("/orders/restaurant/**").hasRole("RESTAURANT_EMPLOYEE") // Restaurant order endpoints
                        .anyRequest().authenticated() // Protect all other endpoints
                )

                // Configure login behavior
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"message\": \"Login successful\", \"username\": \"" + authentication.getName() + "\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\": \"Invalid credentials\"}");
                        })
                        .permitAll()
                )

                // Configure logout behavior
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(this.customLogoutHandler())
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            if (authentication == null || "anonymousUser".equals(authentication.getName())) {
                                response.setStatus(400);
                                response.getWriter().write("{\"message\": \"No user was logged in\"}");
                            } else {
                                String username = authentication.getName();
                                response.getWriter().write("{\"message\": \"Logout successful\", \"user\": \"" + username + "\"}");
                            }
                        })
                        .permitAll()
                )

                // Configure behavior for unauthorized requests
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\": \"Authentication required\"}");
                        })
                );

        return http.build();
    }

    /**
     * Handles custom logout logic, such as token blacklisting.
     *
     * @return A LogoutHandler bean.
     */
    @Bean
    public LogoutHandler customLogoutHandler() {
        return (request, response, authentication) -> {
            String tokenHeader = request.getHeader("Authorization");
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.replace("Bearer ", "");
                this.tokenBlacklistService.blacklistToken(token);
            }
        };
    }

    /**
     * Password encoder bean using BCrypt hashing.
     *
     * @return A PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the authentication manager with custom user details and password encoder.
     *
     * @param http The HttpSecurity object.
     * @return A configured AuthenticationManager.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authManagerBuilder.build();
    }
}
