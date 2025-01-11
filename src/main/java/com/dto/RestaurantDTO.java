package com.dto;

import com.model.Restaurant;

import java.util.List;
import java.util.stream.Collectors;

public class RestaurantDTO {
    private String name;
    private String slug;
    private List<MenuItemDTO> menuItems;

    public RestaurantDTO(Restaurant restaurant) {
        this.name = restaurant.getName();
        this.slug = restaurant.getSlug();
        this.menuItems = restaurant.getMenuItems() == null ? null :
                restaurant.getMenuItems().stream().map(MenuItemDTO::new).collect(Collectors.toList());
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public List<MenuItemDTO> getMenuItems() {
        return menuItems;
    }
}
