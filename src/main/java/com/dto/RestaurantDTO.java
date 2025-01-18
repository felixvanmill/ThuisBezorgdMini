package com.dto;

import com.model.Restaurant;
import com.model.MenuItem;

import java.util.List;
import java.util.stream.Collectors;

public class RestaurantDTO {
    private String name;
    private String slug;
    private List<MenuItemDTO> menuItems;

    public RestaurantDTO(Restaurant restaurant, List<MenuItem> menuItems, boolean includeInventory) {
        this.name = restaurant.getName();
        this.slug = restaurant.getSlug();
        this.menuItems = menuItems.stream()
                .map(menuItem -> new MenuItemDTO(menuItem, includeInventory)) // Pass the includeInventory flag
                .collect(Collectors.toList());
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public List<MenuItemDTO> getMenuItems() {
        return menuItems;
    }

    // Setter for menuItems
    public void setMenuItems(List<MenuItem> menuItems, boolean includeInventory) {
        this.menuItems = menuItems.stream()
                .map(menuItem -> new MenuItemDTO(menuItem, includeInventory)) // Pass the includeInventory flag
                .collect(Collectors.toList());
    }
}
