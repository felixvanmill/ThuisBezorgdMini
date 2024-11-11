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

    @OneToOne(cascade = CascadeType.ALL)  // CascadeType.ALL ensures the Address is persisted as part of the CustomerOrder
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    private String status;
    private double totalPrice;


    // Constructors
    public CustomerOrder() {
    }

    public CustomerOrder(AppUser user, List<MenuItem> menuItems, Address address, String status, double totalPrice) {
        this.user = user;
        this.menuItems = menuItems;
        this.address = address;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    // Getters en Setters
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
}
