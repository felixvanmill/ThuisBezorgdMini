package com.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToMany
    @JoinTable(
            name = "customer_order_menu_item",
            joinColumns = @JoinColumn(name = "customer_order_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private List<MenuItem> menuItems;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private String status;
    private double totalPrice;
    private String deliveryPersonUsername;

    // Constructors, getters, and setters
    public CustomerOrder() {
    }

    public CustomerOrder(AppUser user, List<MenuItem> menuItems, Address address, String status, double totalPrice, String deliveryPersonUsername, Restaurant restaurant) {
        this.user = user;
        this.menuItems = menuItems;
        this.address = address;
        this.status = status;
        this.totalPrice = totalPrice;
        this.deliveryPersonUsername = deliveryPersonUsername;
        this.restaurant = restaurant;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDeliveryPersonUsername() {
        return deliveryPersonUsername;
    }

    public void setDeliveryPersonUsername(String deliveryPersonUsername) {
        this.deliveryPersonUsername = deliveryPersonUsername;
    }
}
