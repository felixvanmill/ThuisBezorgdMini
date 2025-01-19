package com.repository;

import com.model.Restaurant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for managing Restaurant entities.
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Fetch a restaurant by slug with menu items eagerly loaded.
     *
     * @param slug The slug of the restaurant.
     * @return Optional containing the restaurant with menu items, if found.
     */
    @EntityGraph(attributePaths = "menuItems")
    Optional<Restaurant> findBySlug(String slug);

    /**
     * Fetch a restaurant by ID with both employees and menu items eagerly loaded.
     *
     * @param id The ID of the restaurant.
     * @return Optional containing the restaurant with employees and menu items, if found.
     */
    @EntityGraph(attributePaths = {"menuItems", "employees"})
    @Override
    Optional<Restaurant> findById(Long id);

    /**
     * Fetch a restaurant by slug with full details (menu items and employees).
     *
     * @param slug The slug of the restaurant.
     * @return Optional containing the restaurant with full details, if found.
     */
    @EntityGraph(attributePaths = {"menuItems", "employees"})
    @Query("SELECT r FROM Restaurant r WHERE r.slug = :slug")
    Optional<Restaurant> findBySlugWithDetails(@Param("slug") String slug);

    /**
     * Fetch a restaurant for an employee by username with menu items and employees eagerly loaded.
     *
     * @param username The username of the employee.
     * @return Optional containing the restaurant, if found.
     */
    @EntityGraph(attributePaths = {"menuItems", "employees"})
    Optional<Restaurant> findByEmployees_Username(String username);

    /**
     * Fetch a restaurant by slug with employees eagerly loaded.
     *
     * @param slug The slug of the restaurant.
     * @return Optional containing the restaurant with employees, if found.
     */
    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.employees WHERE r.slug = :slug")
    Optional<Restaurant> findBySlugWithEmployees(@Param("slug") String slug);
}
