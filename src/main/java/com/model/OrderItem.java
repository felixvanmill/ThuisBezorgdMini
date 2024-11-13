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

    public OrderItem(MenuItem menuItem, int quantity, String orderNumber) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.orderNumber = orderNumber;
    }

    public double getTotalPrice() {
        return menuItem.getPrice() * quantity;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
