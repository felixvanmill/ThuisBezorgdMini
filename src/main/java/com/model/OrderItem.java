package com.model;

import jakarta.persistence.*;

/**
 * Represents an item within a customer order.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate primary key
    private Long id;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false) // Link to the menu item
    private MenuItem menuItem;

    @Column(nullable = false) // Quantity is required
    private int quantity;

    @Column(name = "order_number", nullable = false) // Order number reference
    private String orderNumber;

    // Default constructor required by JPA
    public OrderItem() {}

    /**
     * Constructs an order item.
     *
     * @param menuItem   The associated menu item.
     * @param quantity   The quantity of the item.
     * @param orderNumber The associated order number.
     */
    public OrderItem(MenuItem menuItem, int quantity, String orderNumber) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.orderNumber = orderNumber;
    }

    /**
     * Calculates the total price for this order item.
     *
     * @return The total price (price per item * quantity).
     */
    public double getTotalPrice() {
        return menuItem.getPrice() * quantity;
    }

    // Getters and Setters

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
