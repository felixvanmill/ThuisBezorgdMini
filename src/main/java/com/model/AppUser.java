package com.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Ensure usernames are unique

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // Role can be CUSTOMER, RESTAURANT_EMPLOYEE, DELIVERY_PERSON

    private String fullName;

    @ManyToOne
    @JoinColumn(name = "restaurant_id") // Foreign key to the restaurant
    private Restaurant restaurant; // Association to a restaurant (for employees)

    // Default constructor
    public AppUser() {
    }

    // Constructor for creating a user without a restaurant association
    public AppUser(String username, String password, String role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    // Constructor for creating a user with a restaurant association
    public AppUser(String username, String password, String role, String fullName, Restaurant restaurant) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.restaurant = restaurant;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
