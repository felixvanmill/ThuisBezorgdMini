package com.repository;

import com.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing MenuItem entities.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {


    /**
     * Fetch menu items by availability status for a restaurant.
     *
     * @param restaurantId The ID of the restaurant.
     * @param isAvailable  The availability status.
     * @return List of available or unavailable menu items based on the parameter.
     */
    List<MenuItem> findByRestaurant_IdAndIsAvailable(Long restaurantId, boolean isAvailable);

    /**
     * Alias method for findById to make it explicitly visible in the repository.
     *
     * @param id The ID of the menu item.
     * @return The menu item, if found.
     */
    Optional<MenuItem> findMenuItemById(Long id);

    /**
     * Uses the inherited findById method from JpaRepository to find a MenuItem by its ID.
     *
     * @param id The ID of the menu item.
     * @return The menu item, if found.
     * @see org.springframework.data.jpa.repository.JpaRepository#findById(Object)
     */
}
