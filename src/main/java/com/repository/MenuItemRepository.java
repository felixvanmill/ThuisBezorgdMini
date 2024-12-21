package com.repository;

import com.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Find menu items by restaurant ID
    List<MenuItem> findByRestaurant_Id(Long restaurantId);

    // Find available menu items by restaurant
    List<MenuItem> findByRestaurant_IdAndInventoryGreaterThan(Long restaurantId, int inventoryThreshold);

    // Update inventory by ID
    @Modifying
    @Query("UPDATE MenuItem m SET m.inventory = m.inventory + :quantity WHERE m.id = :menuItemId")
    void addInventory(@Param("menuItemId") Long menuItemId, @Param("quantity") int quantity);
}
