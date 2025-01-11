package com.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String location;

    @Column(unique = true, nullable = false)
    private String slug; // Field for URL-friendly name

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<MenuItem> menuItems;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<AppUser> employees;

    // Constructors
    public Restaurant() {}

    public Restaurant(final String name, final String description, final String location) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.slug = generateSlug(name); // Automatically generate slug
    }

    // Lifecycle callback to ensure slug is generated before persisting
    @PrePersist
    private void prePersistSlug() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = generateSlug(this.name);
        }
    }

    // Slug generation logic
    private String generateSlug(final String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-") // Replace non-alphanumeric characters with hyphens
                .replaceAll("-$", "");       // Remove trailing hyphens
    }

    public void setName(final String name) {
        this.name = name;
        this.slug = generateSlug(name); // Update slug when name changes
    }

    // Getters and setters
    public Long getId() {
        return this.id;
    }

    public String getSlug() {
        return this.slug;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public List<MenuItem> getMenuItems() {
        return this.menuItems;
    }

    public void setMenuItems(final List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public List<AppUser> getEmployees() {
        return this.employees;
    }

    public void setEmployees(final List<AppUser> employees) {
        this.employees = employees;
    }
}
