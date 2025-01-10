package com.model;

public enum UserRole {
    CUSTOMER("ROLE_CUSTOMER"),
    RESTAURANT_EMPLOYEE("ROLE_RESTAURANT_EMPLOYEE"),
    DELIVERY_PERSON("ROLE_DELIVERY_PERSON");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
