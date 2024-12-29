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

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;


    // Default constructor
    public AppUser() {
    }

    // Constructor for creating a user without a restaurant association
    public AppUser(final String username, final String password, final String role, final String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    // Constructor for creating a user with a restaurant association
    public AppUser(final String username, final String password, final String role, final String fullName, final Restaurant restaurant) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.restaurant = restaurant;
    }

    // Getters and Setters
    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setRestaurant(final Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    public Address getAddress() {
        return this.address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

}
