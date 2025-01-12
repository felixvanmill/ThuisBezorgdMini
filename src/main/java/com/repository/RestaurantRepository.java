package com.repository;

import com.model.Restaurant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Fetch a restaurant by slug with menuItems eagerly loaded.
     */
    @EntityGraph(attributePaths = {"menuItems"})
    Optional<Restaurant> findBySlug(String slug);

    /**
     * Fetch a restaurant with both employees and menu items by slug.
     */
    @EntityGraph(attributePaths = {"menuItems", "employees"})
    @Override
    Optional<Restaurant> findById(Long id);

    /**
     * Custom query for fetching restaurant by slug with full details.
     */
    @EntityGraph(attributePaths = {"menuItems", "employees"})
    @Query("SELECT r FROM Restaurant r WHERE r.slug = :slug")
    Optional<Restaurant> findBySlugWithDetails(@Param("slug") String slug);

    /**
     * Fetch a restaurant for an employee by username with collections initialized.
     */
    @EntityGraph(attributePaths = {"menuItems", "employees"})
    Optional<Restaurant> findByEmployees_Username(String username);
}
