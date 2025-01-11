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

    @OneToOne(fetch = FetchType.EAGER) // Avoid cascading changes to Address
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
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
        orderNumber = this.generateOrderNumber();
    }

    // Constructor to initialize all fields
    public CustomerOrder(final AppUser user, final List<OrderItem> orderItems, final Address address, final OrderStatus status, final double totalPrice, final Restaurant restaurant) {
        orderNumber = this.generateOrderNumber();
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
        return this.orderItems.stream().mapToDouble(OrderItem::getTotalPrice).sum();
    }

    // Getters and Setters
    public Long getId() {
        return this.id;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public AppUser getUser() {
        return this.user;
    }

    public void setUser(final AppUser user) {
        this.user = user;
    }

    public List<OrderItem> getOrderItems() {
        return this.orderItems;
    }

    public void setOrderItems(final List<OrderItem> orderItems) {
        orderItems.forEach(item -> item.setOrderNumber(orderNumber));
        this.orderItems = orderItems;
        totalPrice = this.calculateTotalPrice();
    }

    public double getTotalPrice() {
        return this.totalPrice;
    }

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public void setStatus(final OrderStatus status) {
        this.status = status;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setRestaurant(final Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
