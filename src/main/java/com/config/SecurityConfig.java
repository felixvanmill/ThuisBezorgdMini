package com.config;

import com.service.CustomUserDetailsService;
import com.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityConfig(final CustomUserDetailsService userDetailsService, final TokenBlacklistService tokenBlacklistService) {
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/api/customer/restaurants", "/error").permitAll()
                        .requestMatchers("/auth/logout").authenticated() // Require authentication for logout
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        .requestMatchers("/restaurant/**").hasRole("RESTAURANT_EMPLOYEE")
                        .requestMatchers("/delivery/**").hasRole("DELIVERY_PERSON")
                        .anyRequest().authenticated()
                )
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
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(customLogoutHandler()) // Add custom logout handler
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            // Check if the user is logged in
                            if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
                                response.setStatus(400); // Bad Request
                                response.getWriter().write("{\"message\": \"No user was logged in\"}");
                                return;
                            }

                            // Include user-specific details in the response
                            String username = authentication.getName();
                            response.getWriter().write("{\"message\": \"Logout successful\", \"user\": \"" + username + "\"}");
                        })
                        .permitAll()
                )
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

    @Bean
    public LogoutHandler customLogoutHandler() {
        return (request, response, authentication) -> {
            // Extract token from Authorization header
            String tokenHeader = request.getHeader("Authorization");
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.replace("Bearer ", "");
                tokenBlacklistService.blacklistToken(token); // Blacklist the token
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(final HttpSecurity http) throws Exception {
        final AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(this.passwordEncoder());
        return authManagerBuilder.build();
    }
}
