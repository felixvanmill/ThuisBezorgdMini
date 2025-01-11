package com.repository;

import com.model.Restaurant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @EntityGraph(attributePaths = {"menuItems"})
    Optional<Restaurant> findBySlug(String slug);

    // Find a restaurant by employee's username
    Optional<Restaurant> findByEmployees_Username(String username);

}
