package com.model;

import jakarta.persistence.*;

/**
 * Represents a menu item in a restaurant.
 */
@Entity
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate primary key
    private Long id;

    private String name;
    private String description;
    private double price;
    private String ingredients;

    @ManyToOne
    @JoinColumn(name = "restaurant_id") // Link to the restaurant
    private Restaurant restaurant;

    @Column(nullable = false) // Inventory must always have a value
    private int inventory = 999; // Default inventory value

    @Column(name = "is_available", nullable = false) // Indicates if the item is available
    private boolean isAvailable = true;

    // Constructors
    public MenuItem() {
    }

    public MenuItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public MenuItem(String name, String description, double price, String ingredients, Restaurant restaurant) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ingredients = ingredients;
        this.restaurant = restaurant;
    }

    public MenuItem(String name, String description, double price, String ingredients, Restaurant restaurant, int inventory) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ingredients = ingredients;
        this.restaurant = restaurant;
        this.inventory = inventory;
    }

    // Getters and Setters
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

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    /**
     * Reduces the inventory by the specified quantity.
     *
     * @param quantity The quantity to reduce.
     * @throws IllegalStateException if inventory is insufficient.
     */
    public void reduceInventory(int quantity) {
        if (this.inventory >= quantity) {
            this.inventory -= quantity;
        } else {
            throw new IllegalStateException("Insufficient inventory for " + this.name);
        }
    }

    /**
     * Sets the restaurant ID by creating or updating the associated restaurant entity.
     *
     * @param restaurantId The ID of the restaurant.
     */
    public void setRestaurantId(Long restaurantId) {
        if (this.restaurant == null) {
            this.restaurant = new Restaurant();
        }
        this.restaurant.setId(restaurantId);
    }
}
