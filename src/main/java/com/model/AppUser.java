package com.model;

import jakarta.persistence.*;

/**
 * Entity class representing an application user.
 * Each user has a role, which is now managed as an enum (UserRole).
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate primary key values
    private Long id;

    @Column(nullable = false, unique = true) // Ensure usernames are unique and required
    private String username;

    @Column(nullable = false) // Passwords are required
    private String password;

    @Enumerated(EnumType.STRING) // Store the UserRole enum as a string in the database
    @Column(nullable = false) // Roles are required
    private UserRole role;

    private String fullName; // Optional full name for the user

    @ManyToOne
    @JoinColumn(name = "restaurant_id") // Foreign key to associate user with a restaurant
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "address_id") // Foreign key to associate user with an address
    private Address address;

    // Default constructor (required by JPA)
    public AppUser() {
    }

    /**
     * Constructor for creating a user without a restaurant association.
     *
     * @param username  The user's unique username.
     * @param password  The user's password.
     * @param role      The user's role as a UserRole enum.
     * @param fullName  The user's full name.
     */
    public AppUser(final String username, final String password, final UserRole role, final String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    /**
     * Constructor for creating a user with a restaurant association.
     *
     * @param username   The user's unique username.
     * @param password   The user's password.
     * @param role       The user's role as a UserRole enum.
     * @param fullName   The user's full name.
     * @param restaurant The restaurant associated with the user.
     */
    public AppUser(final String username, final String password, final UserRole role, final String fullName, final Restaurant restaurant) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.restaurant = restaurant;
    }

    // Getters and setters for accessing and modifying fields

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

    public UserRole getRole() { // Return the user's role as a UserRole enum
        return this.role;
    }

    public void setRole(final UserRole role) { // Set the user's role as a UserRole enum
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
