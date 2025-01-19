package com.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Represents a restaurant entity with menu items and employees.
 */
@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate primary key
    private Long id;

    @Column(nullable = false) // Ensure name is required
    private String name;

    private String description;
    private String location;

    @Column(unique = true, nullable = false) // Ensure slug is unique and required
    private String slug;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppUser> employees;

    // Default constructor (required by JPA)
    public Restaurant() {}

    /**
     * Constructs a restaurant with the given details.
     *
     * @param name        Name of the restaurant.
     * @param description Description of the restaurant.
     * @param location    Location of the restaurant.
     */
    public Restaurant(String name, String description, String location) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.slug = generateSlug(name);
    }

    // Lifecycle callback to generate slug before persisting
    @PrePersist
    private void prePersistSlug() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = generateSlug(this.name);
        }
    }

    // Generate a URL-friendly slug from the restaurant name
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-") // Replace non-alphanumeric characters with hyphens
                .replaceAll("-$", "");       // Remove trailing hyphens
    }

    public void setName(String name) {
        this.name = name;
        this.slug = generateSlug(name); // Update slug when name changes
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public List<AppUser> getEmployees() {
        return employees;
    }

    public void setEmployees(List<AppUser> employees) {
        this.employees = employees;
    }

    public void setSlug(String slug) {
        this.slug = slug; // Add this setter
    }
}
