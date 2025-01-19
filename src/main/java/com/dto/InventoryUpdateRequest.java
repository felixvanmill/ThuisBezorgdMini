package com.dto;

/**
 * DTO for updating inventory of a menu item.
 */
public class InventoryUpdateRequest {

    private Long menuItemId;
    private int quantity;

    // Getters and setters
    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
