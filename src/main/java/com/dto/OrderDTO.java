package com.dto;

import com.model.OrderStatus;
import jakarta.validation.constraints.*;

/**
 * Data Transfer Object for Order details.
 * Represents essential order information for export or API responses.
 */
public class OrderDTO {

    @NotBlank(message = "Ordernummer is verplicht")
    private String orderNumber; // Unique identifier for the order

    @Positive(message = "Totale prijs moet een positief getal zijn")
    private double totalPrice;  // Total price of the order

    @NotNull(message = "Status is verplicht")
    private OrderStatus status; // Current status of the order

    @NotBlank(message = "Klantnaam is verplicht")
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

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
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