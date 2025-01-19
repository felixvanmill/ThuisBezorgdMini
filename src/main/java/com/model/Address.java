package com.model;

import jakarta.persistence.*;

/**
 * Entity representing an address.
 */
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String streetName;
    private String houseNumber;
    private String postalCode;
    private String city;

    // Default constructor
    public Address() {
    }

    // Constructor with fields
    public Address(String streetName, String houseNumber, String postalCode, String city) {
        this.streetName = streetName;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.city = city;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Returns the full address as a single string.
     *
     * @return Full address in the format: "streetName houseNumber, postalCode city"
     */
    public String getFullAddress() {
        return streetName + " " + houseNumber + ", " + postalCode + " " + city;
    }
}
