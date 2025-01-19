package com.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.model.MenuItem;

/**
 * DTO for transferring menu item data.
 */
public class MenuItemDTO {

    private Long id;
    private String name;
    private String description;
    private double price;
    private boolean isAvailable;

    @JsonInclude(JsonInclude.Include.NON_NULL) // Exclude if null
    private Integer inventory; // Null if inventory is not included

    // No-args constructor for serialization/deserialization
    public MenuItemDTO() {
    }

    /**
     * Constructs a MenuItemDTO from a MenuItem entity.
     *
     * @param menuItem         The MenuItem entity (cannot be null).
     * @param includeInventory Whether to include inventory in the DTO.
     */
    public MenuItemDTO(MenuItem menuItem, boolean includeInventory) {
        if (menuItem == null) {
            throw new IllegalArgumentException("MenuItem cannot be null");
        }
        this.id = menuItem.getId();
        this.name = menuItem.getName();
        this.description = menuItem.getDescription();
        this.price = menuItem.getPrice();
        this.isAvailable = menuItem.isAvailable();
        this.inventory = includeInventory ? menuItem.getInventory() : null;
    }

    // Getters and setters for serialization/deserialization
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public Integer getInventory() {
        return inventory;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }
}
