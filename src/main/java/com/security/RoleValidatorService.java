package com.security;

import com.model.AppUser;
import com.model.Restaurant;
import com.model.UserRole;
import org.springframework.stereotype.Service;

@Service
public class RoleValidatorService {

    /**
     * Validates if a user is a restaurant employee managing a specific restaurant.
     * @param user the authenticated user.
     * @param restaurant the restaurant being accessed.
     * @return true if the user is authorized to manage the restaurant.
     */
    public boolean validateRestaurantEmployee(AppUser user, Restaurant restaurant) {
        return user.getRole() == UserRole.RESTAURANT_EMPLOYEE &&
                restaurant != null &&
                restaurant.getEmployees().contains(user);
    }

    /**
     * Validates if a user is a delivery person.
     * @param user the authenticated user.
     * @return true if the user has the delivery person role.
     */
    public boolean validateDeliveryPerson(AppUser user) {
        return user.getRole() == UserRole.DELIVERY_PERSON;
    }

    /**
     * Validates if a user is a customer.
     * @param user the authenticated user.
     * @return true if the user has the customer role.
     */
    public boolean validateCustomer(AppUser user) {
        return user.getRole() == UserRole.CUSTOMER;
    }

    /**
     * Validates if a user has access to a resource based on roles.
     * @param user the authenticated user.
     * @param requiredRole the role required to access the resource.
     * @return true if the user has the required role.
     */
    public boolean validateRole(AppUser user, UserRole requiredRole) {
        return user.getRole() == requiredRole;
    }
}
