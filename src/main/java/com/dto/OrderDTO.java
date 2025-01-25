package com.dto;

import com.model.OrderStatus;

/**
 * Data Transfer Object for Order details.
 * Represents essential order information for export or API responses.
 */
public class OrderDTO {

    private String orderNumber; // Unique identifier for the order
    private double totalPrice;  // Total price of the order
    private OrderStatus status; // Current status of the order
    private String customer;    // Customer's full name
    private String items;       // Description of order items (optional)

    /**
     * Constructs an OrderDTO with the specified details.
     *
     * @param orderNumber The unique identifier of the order
     * @param totalPrice  The total price of the order
     * @param status      The current status of the order
     * @param customer    The name of the customer
     * @param items       The order items description
     */
    public OrderDTO(String orderNumber, double totalPrice, OrderStatus status, String customer, String items) {
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.status = status;
        this.customer = customer;
        this.items = items;
    }

    // Getters and Setters

    public String getOrderNumber() {
        return orderNumber;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getCustomer() {
        return customer;
    }

    public String getItems() {
        return items;
    }

    /**
     * Updates the items description for the order.
     *
     * @param items The updated order items description
     */
    public void setItems(String items) {
        this.items = items;
    }
}
