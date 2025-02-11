package com.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * Represents an application user with roles and optional associations to a restaurant and address.
 */


@Entity
@Table(name = "app_users")
@JsonIgnoreProperties({"password"})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate primary key
    private Long id;

    @Column(nullable = false, unique = true) // Username must be unique and not null
    private String username;

    @Column(nullable = false) // Password is required
    private String password;

    @Enumerated(EnumType.STRING) // Store role as a string in the database
    @Column(nullable = false) // Role is required
    private UserRole role;

    private String fullName; // Optional full name

    @ManyToOne
    @JoinColumn(name = "restaurant_id") // Link to a restaurant (optional)
    private Restaurant restaurant;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", unique = true, nullable = false)
    private Address address;


    // Default constructor required by JPA
    public AppUser() {
    }

    // Constructor for a user without a restaurant
    public AppUser(String username, String password, UserRole role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    // Constructor for a user with a restaurant
    public AppUser(String username, String password, UserRole role, String fullName, Restaurant restaurant) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.restaurant = restaurant;
    }

    // Getters and setters
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
