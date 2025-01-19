package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.MenuItem;

/**
 * DTO for transferring menu item data.
 */
public class MenuItemDTO {

    private final Long id;
    private final String name;
    private final String description;
    private final double price;
    private final boolean isAvailable;

    @JsonInclude(JsonInclude.Include.NON_NULL) // Exclude if null
    private final Integer inventory; // Null if inventory is not included

    /**
     * Constructs a MenuItemDTO from a MenuItem entity.
     *
     * @param menuItem         The MenuItem entity.
     * @param includeInventory Whether to include inventory in the DTO.
     */
    public MenuItemDTO(MenuItem menuItem, boolean includeInventory) {
        this.id = menuItem.getId(); // Initialize ID from MenuItem
        this.name = menuItem.getName();
        this.description = menuItem.getDescription();
        this.price = menuItem.getPrice();
        this.isAvailable = menuItem.isAvailable();
        this.inventory = includeInventory ? menuItem.getInventory() : null;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public Integer getInventory() {
        return inventory;
    }
}
