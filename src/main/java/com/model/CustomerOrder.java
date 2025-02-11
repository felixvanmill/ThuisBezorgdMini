package com.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.UUID;

/**
 * Represents a customer's order, including items, delivery address, and status.
 */
@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate primary key
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false) // Unique order identifier
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Link to the customer
    private AppUser user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_order_id") // Link items to the order
    @JsonIgnore
    private List<OrderItem> orderItems;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", nullable = false) // Delivery address
    private Address address;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false) // Associated restaurant
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // Order status
    private OrderStatus status;

    @Column(name = "total_price") // Total order price
    private double totalPrice;

    @Column(name = "delivery_person") // Assigned delivery person
    private String deliveryPerson;

    // Default constructor
    public CustomerOrder() {
        this.orderNumber = generateOrderNumber();
    }



    // Constructor with fields
    public CustomerOrder(AppUser user, List<OrderItem> orderItems, Address address, OrderStatus status, double totalPrice, Restaurant restaurant) {
        this.orderNumber = generateOrderNumber();
        this.user = user;
        this.orderItems = orderItems;
        this.address = address;
        this.status = status;
        this.totalPrice = totalPrice;
        this.restaurant = restaurant;
    }

    // Generate a unique order number
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Calculate total price based on items
    public double calculateTotalPrice() {
        return orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getDeliveryPerson() {
        return deliveryPerson;
    }

    public void setDeliveryPerson(String deliveryPerson) {
        this.deliveryPerson = deliveryPerson;
    }

    public AppUser getCustomer() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }



}
