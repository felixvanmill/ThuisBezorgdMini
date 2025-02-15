package com.utils;

import com.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    private static AppUserRepository appUserRepository;

    @Autowired
    public AuthUtils(AppUserRepository repository) {
        appUserRepository = repository;
    }

    public static boolean isRestaurantEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_RESTAURANT_EMPLOYEE"));
    }

    public static String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    public static Long getAuthenticatedRestaurantId(String username) {
        return appUserRepository.findByUsername(username)
                .map(user -> user.getRestaurant() != null ? user.getRestaurant().getId() : null)
                .orElse(null);
    }

    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


}
