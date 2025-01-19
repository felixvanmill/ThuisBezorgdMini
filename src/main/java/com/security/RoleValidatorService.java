package com.security;

import com.model.AppUser;
import com.model.Restaurant;
import com.model.UserRole;
import org.springframework.stereotype.Service;

/**
 * Service for validating user roles and access permissions.
 */
@Service
public class RoleValidatorService {

    /**
     * Checks if the user is a restaurant employee managing the given restaurant.
     *
     * @param user       The logged-in user.
     * @param restaurant The restaurant to check.
     * @return True if the user is authorized for this restaurant.
     */
    public boolean validateRestaurantEmployee(final AppUser user, final Restaurant restaurant) {
        return UserRole.RESTAURANT_EMPLOYEE == user.getRole()
                && restaurant != null
                && restaurant.getEmployees().contains(user);
    }

    /**
     * Checks if the user is a delivery person.
     *
     * @param user The logged-in user.
     * @return True if the user is a delivery person.
     */
    public boolean validateDeliveryPerson(final AppUser user) {
        return UserRole.DELIVERY_PERSON == user.getRole();
    }

    /**
     * Checks if the user is a customer.
     *
     * @param user The logged-in user.
     * @return True if the user is a customer.
     */
    public boolean validateCustomer(final AppUser user) {
        return UserRole.CUSTOMER == user.getRole();
    }

    /**
     * Checks if the user has the required role to access a resource.
     *
     * @param user         The logged-in user.
     * @param requiredRole The required role for access.
     * @return True if the user has the required role.
     */
    public boolean validateRole(final AppUser user, final UserRole requiredRole) {
        return user.getRole() == requiredRole;
    }
}
