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
    private String slug; // New field for URL-friendly name

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<MenuItem> menuItems;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<AppUser> employees;

    // Constructors
    public Restaurant() {}

    public Restaurant(String name, String description, String location) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.slug = generateSlug(name); // Automatically generate slug
    }

    // Slug generation logic
    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-$", "");
    }

    public void setName(String name) {
        this.name = name;
        this.slug = generateSlug(name); // Update slug when name changes
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
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
}
