package com.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    private int quantity;

    // Add reference to the order number
    @Column(name = "order_number")
    private String orderNumber;

    public OrderItem() {}

    public OrderItem(final MenuItem menuItem, final int quantity, final String orderNumber) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.orderNumber = orderNumber;
    }

    public double getTotalPrice() {
        return this.menuItem.getPrice() * this.quantity;
    }

    // Getters and setters

    public Long getId() {
        return this.id;
    }

    public MenuItem getMenuItem() {
        return this.menuItem;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
