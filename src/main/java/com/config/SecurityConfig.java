package com.config;

import com.security.JwtAuthorizationFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * Sets up security rules for the app, like login, logout, and protected routes.
 */
@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    /**
     * Constructor to link necessary services.
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          TokenBlacklistService tokenBlacklistService,
                          JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    /**
     * Defines security rules for HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Turn off CSRF protection (not needed for APIs)
                .csrf(csrf -> csrf.disable())

                // Define which endpoints are public or protected
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/error").permitAll() // Public routes
                        .requestMatchers("/customer/**").hasRole("CUSTOMER") // Only customers
                        .requestMatchers("/restaurant/**").hasRole("RESTAURANT_EMPLOYEE") // Only restaurant employees
                        .requestMatchers("/orders/customer/**").hasRole("CUSTOMER") // Orders for customers
                        .requestMatchers("/orders/restaurant/**").hasRole("RESTAURANT_EMPLOYEE") // Orders for restaurants
                        .anyRequest().authenticated() // Everything else needs login
                )

                // Add the JWT filter to check tokens
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)

                // Handle login
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login") // Where the login request is sent
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

                // Handle logout
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // Where the logout request is sent
                        .addLogoutHandler(customLogoutHandler()) // Blacklist the token on logout
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

                // Handle unauthorized access
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
     * Handles custom logic for logout (blacklisting tokens).
     */
    @Bean
    public LogoutHandler customLogoutHandler() {
        return (request, response, authentication) -> {
            String tokenHeader = request.getHeader("Authorization");
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.substring(7); // Remove "Bearer " from the header
                tokenBlacklistService.blacklistToken(token); // Add the token to the blacklist
            }
        };
    }

    /**
     * Creates a password encoder to hash passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt is secure for hashing passwords
    }

    /**
     * Configures authentication with user details and password encoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authManagerBuilder.build();
    }
}
