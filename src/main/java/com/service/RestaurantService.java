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

    public List<Restaurant> getAllRestaurants() {
        return this.restaurantRepository.findAll();
    }

    public Optional<Restaurant> getRestaurantById(final Long id) {
        return this.restaurantRepository.findById(id);
    }

    public Restaurant addRestaurant(final Restaurant restaurant) {
        return this.restaurantRepository.save(restaurant);
    }

    public void deleteRestaurant(final Long id) {
        this.restaurantRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurantsWithMenu() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurants.stream().map(RestaurantDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantWithMenu(String slug) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));
        return new RestaurantDTO(restaurant);
    }

    // Add this method to find a restaurant by its slug
    @Transactional(readOnly = true)
    public Optional<Restaurant> getRestaurantBySlug(String slug) {
        return restaurantRepository.findBySlug(slug);
    }
}
