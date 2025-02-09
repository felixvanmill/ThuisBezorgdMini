package com.dto;

/**
 * DTO for updating inventory of a menu item.
 */
public class InventoryUpdateRequest {

    private Long menuItemId;
    private int quantity;

    public InventoryUpdateRequest() {}

    public InventoryUpdateRequest(Long menuItemId, int quantity) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }

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
