package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.MenuItem;

public class MenuItemDTO {
    private String name;
    private String description;
    private double price;
    private boolean isAvailable;

    @JsonInclude(JsonInclude.Include.NON_NULL) // Exclude if null
    private Integer inventory; // Use Integer to allow null for exclusion

    public MenuItemDTO(MenuItem menuItem, boolean includeInventory) {
        this.name = menuItem.getName();
        this.description = menuItem.getDescription();
        this.price = menuItem.getPrice();
        this.isAvailable = menuItem.isAvailable();

        // Include inventory only if requested
        this.inventory = includeInventory ? menuItem.getInventory() : null;
    }

    // Getters
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
