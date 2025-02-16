package com.config;

import com.security.JwtAuthorizationFilter;
import com.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Voor extra beveiliging op methodeniveau
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityConfig(JwtAuthorizationFilter jwtAuthorizationFilter,
                          TokenBlacklistService tokenBlacklistService) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF voor stateless JWT-authenticatie
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/login", "/auth/login", "/auth/register", "/error").permitAll()

                        // CUSTOMER-
                        .requestMatchers("GET", "/api/v1/restaurants/**").permitAll()
                        .requestMatchers("/api/v1/orders").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/orders/{orderNumber}").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/restaurants/{slug}/orders").hasRole("CUSTOMER")
                        .requestMatchers("PATCH", "/api/v1/orders/{orderNumber}/status").hasRole("CUSTOMER")

                        // RESTAURANT_EMPLOYEE
                        .requestMatchers("/api/v1/restaurants/**").hasRole("RESTAURANT_EMPLOYEE")
                        .requestMatchers("PATCH", "/api/v1/restaurants/{slug}/orders/{orderNumber}/status").hasRole("RESTAURANT_EMPLOYEE")
                        .requestMatchers("POST", "/api/v1/restaurants/{slug}/menu-items/upload").hasRole("RESTAURANT_EMPLOYEE")
                        .requestMatchers("GET", "/api/v1/restaurants/orders/download").hasRole("RESTAURANT_EMPLOYEE")

                        // DELIVERY_PERSON
                        .requestMatchers("GET", "/api/v1/delivery/orders").hasRole("DELIVERY_PERSON")
                        .requestMatchers("GET", "/api/v1/delivery/history").hasRole("DELIVERY_PERSON")
                        .requestMatchers("POST", "/api/v1/delivery/orders/{orderNumber}/assign").hasRole("DELIVERY_PERSON")
                        .requestMatchers("PATCH", "/api/v1/delivery/orders/{orderNumber}/status").hasRole("DELIVERY_PERSON")

                        // ADMIN
                        .requestMatchers("/api/users/**").hasRole("ADMIN") // Beheer van gebruikersaccounts

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(customLogoutHandler())
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"message\": \"Logout successful\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public LogoutHandler customLogoutHandler() {
        return (request, response, authentication) -> {
            String tokenHeader = request.getHeader("Authorization");
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.substring(7);
                tokenBlacklistService.blacklistToken(token);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
