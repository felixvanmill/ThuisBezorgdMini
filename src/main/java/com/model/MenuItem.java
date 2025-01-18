package com.model;

import jakarta.persistence.*;

@Entity
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private String ingredients;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(nullable = false)
    private int inventory = 999; // Default inventory

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true; // Default to true

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }


    // Constructors
    public MenuItem() {
    }

    public MenuItem(final String name, final double price) {
        this.name = name;
        this.price = price;
    }

    public MenuItem(final String name, final String description, final double price, final String ingredients, final Restaurant restaurant) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ingredients = ingredients;
        this.restaurant = restaurant;
    }

    // NEW CONSTRUCTOR INCLUDING INVENTORY
    public MenuItem(final String name, final String description, final double price, final String ingredients, final Restaurant restaurant, final int inventory) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ingredients = ingredients;
        this.restaurant = restaurant;
        this.inventory = inventory;
    }

    // Getters and Setters
    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(final double price) {
        this.price = price;
    }

    public String getIngredients() {
        return this.ingredients;
    }

    public void setIngredients(final String ingredients) {
        this.ingredients = ingredients;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setRestaurant(final Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public int getInventory() {
        return this.inventory;
    }

    public void setInventory(final int inventory) {
        this.inventory = inventory;
    }

    public void reduceInventory(final int quantity) {
        if (inventory >= quantity) {
            inventory -= quantity;
        } else {
            throw new IllegalStateException("Insufficient inventory for " + name);
        }
    }


}
