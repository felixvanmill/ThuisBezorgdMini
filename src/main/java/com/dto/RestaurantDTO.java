package com.dto;

import com.model.Restaurant;
import com.model.MenuItem;

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
     * Constructs a RestaurantDTO with restaurant and menu item details.
     *
     * @param restaurant       The Restaurant entity.
     * @param menuItems        List of menu items associated with the restaurant.
     * @param includeInventory Whether to include inventory in the menu item details.
     */
    public RestaurantDTO(Restaurant restaurant, List<MenuItem> menuItems, boolean includeInventory) {
        this.name = restaurant.getName();
        this.slug = restaurant.getSlug();
        this.menuItems = menuItems.stream()
                .map(menuItem -> new MenuItemDTO(menuItem, includeInventory))
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

    /**
     * Updates the menu items for the restaurant.
     *
     * @param menuItems        List of updated menu items.
     * @param includeInventory Whether to include inventory in the menu item details.
     */
    public void setMenuItems(List<MenuItem> menuItems, boolean includeInventory) {
        this.menuItems = menuItems.stream()
                .map(menuItem -> new MenuItemDTO(menuItem, includeInventory))
                .collect(Collectors.toList());
    }
}
