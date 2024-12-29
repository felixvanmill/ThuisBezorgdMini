package com.service;

import com.model.Restaurant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.repository.RestaurantRepository;

import java.util.List;
import java.util.Optional;

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
}
