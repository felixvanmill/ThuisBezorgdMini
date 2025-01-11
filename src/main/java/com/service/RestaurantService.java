package com.service;

import com.dto.RestaurantDTO;
import com.model.Restaurant;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    /**
     * Fetch all restaurants without menu details.
     */
    @Transactional(readOnly = true)
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Fetch a restaurant by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantById(final Long id) {
        return restaurantRepository.findById(id);
    }

    /**
     * Add a new restaurant to the database.
     */
    public Restaurant addRestaurant(final Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    /**
     * Delete a restaurant by its ID.
     */
    public void deleteRestaurant(final Long id) {
        restaurantRepository.deleteById(id);
    }

    /**
     * Fetch all restaurants with their menu details as DTOs.
     */
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurantsWithMenu() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurants.stream().map(RestaurantDTO::new).collect(Collectors.toList());
    }

    /**
     * Fetch a specific restaurant by its slug with menu details as a DTO.
     */
    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantWithMenu(String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));
        return new RestaurantDTO(restaurant);
    }

    /**
     * Fetch a specific restaurant by its slug without wrapping it in a DTO.
     */
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantBySlug(String slug) {
        return restaurantRepository.findBySlug(slug);
    }
}
