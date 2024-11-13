package com.repository;

import com.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Add this method to find menu items by restaurant ID
    List<MenuItem> findByRestaurant_Id(Long restaurantId);
}
