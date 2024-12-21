package com.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique order number field
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_order_id")
    private List<OrderItem> orderItems;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private String status;

    @Column(name = "total_price")
    private double totalPrice;

    // Default constructor that generates a unique order number
    public CustomerOrder() {
        this.orderNumber = generateOrderNumber();
    }

    // Constructor to initialize all fields, including totalPrice
// CustomerOrder.java

    public CustomerOrder(AppUser user, List<OrderItem> orderItems, Address address, String status, double totalPrice, Restaurant restaurant) {
        this.orderNumber = generateOrderNumber();  // Ensure order number is generated
        this.user = user;
        this.orderItems = orderItems;
        this.address = address;
        this.status = status;
        this.totalPrice = totalPrice;
        this.restaurant = restaurant;
    }


    // Method to generate a unique order number
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public double calculateTotalPrice() {
        return orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        orderItems.forEach(item -> item.setOrderNumber(this.orderNumber));
        this.orderItems = orderItems;
        this.totalPrice = calculateTotalPrice();
    }


    public double getTotalPrice() {
        return totalPrice;
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

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
