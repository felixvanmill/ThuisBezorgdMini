package com.repository;

import com.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // Find a restaurant by employee username
    Optional<Restaurant> findByEmployees_Username(String username);

    // Find a restaurant by slug
    Optional<Restaurant> findBySlug(String slug);

}
