package com.dto;

import com.model.Restaurant;
import com.model.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for transferring restaurant data with optional menu item details.
 */
public class RestaurantDTO {

    private final String name;
    private final String slug;
    private List<MenuItemDTO> menuItems;

    /**
     * ✅ New Simple Constructor for Basic Restaurant → RestaurantDTO Mapping
     */
    public RestaurantDTO(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        this.name = restaurant.getName();
        this.slug = restaurant.getSlug();
        this.menuItems = new ArrayList<>();  // Default empty list
    }

    /**
     * ✅ Full Constructor for RestaurantDTO with Menu Items
     *
     * @param restaurant       The Restaurant entity (cannot be null).
     * @param menuItems        List of menu items associated with the restaurant.
     * @param includeInventory Whether to include inventory in the menu item details.
     */
    public RestaurantDTO(Restaurant restaurant, List<MenuItem> menuItems, boolean includeInventory) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        this.name = restaurant.getName();
        this.slug = restaurant.getSlug();
        this.menuItems = (menuItems != null && !menuItems.isEmpty())
                ? menuItems.stream().map(menuItem -> new MenuItemDTO(menuItem, includeInventory)).toList()
                : new ArrayList<>();
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

    /**
     * Updates the menu items for the restaurant.
     *
     * @param menuItems        List of updated menu items.
     * @param includeInventory Whether to include inventory in the menu item details.
     */
    public void setMenuItems(List<MenuItem> menuItems, boolean includeInventory) {
        this.menuItems = menuItems.stream()
                .filter(item -> item != null) // Ensure no null MenuItem objects
                .map(menuItem -> new MenuItemDTO(menuItem, includeInventory))
                .collect(Collectors.toList());
    }
}
