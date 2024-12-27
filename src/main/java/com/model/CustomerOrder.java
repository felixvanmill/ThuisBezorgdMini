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

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_order_id")
    private List<OrderItem> orderItems;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING) // Maps enum to string in DB
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_price")
    private double totalPrice;

    // Default constructor
    public CustomerOrder() {
        this.orderNumber = generateOrderNumber();
    }

    // Constructor to initialize all fields
    public CustomerOrder(AppUser user, List<OrderItem> orderItems, Address address, OrderStatus status, double totalPrice, Restaurant restaurant) {
        this.orderNumber = generateOrderNumber();
        this.user = user;
        this.orderItems = orderItems;
        this.address = address;
        this.status = status;
        this.totalPrice = totalPrice;
        this.restaurant = restaurant;
    }

    // Generate unique order number
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Calculate total price from order items
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
}
