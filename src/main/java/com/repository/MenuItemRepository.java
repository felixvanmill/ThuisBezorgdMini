package com.repository;

import com.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing MenuItem entities.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Fetch menu items by restaurant ID.
     *
     * @param restaurantId The ID of the restaurant.
     * @return List of menu items belonging to the restaurant.
     */
    List<MenuItem> findByRestaurant_Id(Long restaurantId);

    /**
     * Fetch menu items with inventory above a specific threshold for a restaurant.
     *
     * @param restaurantId      The ID of the restaurant.
     * @param inventoryThreshold The inventory threshold.
     * @return List of menu items meeting the inventory criteria.
     */
    List<MenuItem> findByRestaurant_IdAndInventoryGreaterThan(Long restaurantId, int inventoryThreshold);

    /**
     * Increment the inventory of a menu item by a specified quantity.
     *
     * @param menuItemId The ID of the menu item.
     * @param quantity   The quantity to add.
     */
    @Modifying
    @Query("UPDATE MenuItem m SET m.inventory = m.inventory + :quantity WHERE m.id = :menuItemId")
    void addInventory(@Param("menuItemId") Long menuItemId, @Param("quantity") int quantity);

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
}
