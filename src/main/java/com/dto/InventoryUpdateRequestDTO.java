package com.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for updating inventory of a menu item.
 */
public class InventoryUpdateRequestDTO {

    @NotNull(message = "MenuItem ID is verplicht")
    private Long menuItemId;

    @Min(value = 1, message = "Aantal moet minimaal 1 zijn")
    private int quantity;

    public InventoryUpdateRequestDTO() {}

    public InventoryUpdateRequestDTO(Long menuItemId, int quantity) {
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
