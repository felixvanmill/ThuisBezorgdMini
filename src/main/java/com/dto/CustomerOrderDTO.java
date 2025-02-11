package com.dto;

import com.model.CustomerOrder;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for transferring customer order data.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON
public class CustomerOrderDTO {

    private Long id;
    private String orderNumber;
    private double totalPrice;
    private String status;
    private String restaurantName;
    private String customerName;
    private String customerAddress; // ✅ Add this field

    // Default constructor
    public CustomerOrderDTO() {
    }

    public CustomerOrderDTO(CustomerOrder order) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus().name();
        this.restaurantName = (order.getRestaurant() != null) ? order.getRestaurant().getName() : null;

        this.customerName = (order.getUser() != null) ? order.getUser().getFullName() : "Unknown";
        this.customerAddress = (order.getUser() != null && order.getUser().getAddress() != null)
                ? order.getUser().getAddress().getFullAddress() : "No address available";

    }



    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }
}
