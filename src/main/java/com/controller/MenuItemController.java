package com.controller;

import com.dto.InventoryUpdateRequestDTO;
import com.model.MenuItem;
import com.service.MenuItemService;
import com.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles menu item operations, including CRUD actions, inventory updates, and CSV uploads.
 */
@RestController
@RequestMapping("api/v1/restaurants/{slug}/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    /**
     * Constructor-based Dependency Injection
     */
    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    /**
     * Fetches all menu items.
     */
    @GetMapping
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<List<MenuItem>> getAllMenuItems() {
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }

    /**
     * Fetches a specific menu item by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        return ResponseUtils.handleRequest(() -> menuItemService.getMenuItemById(id));
    }

    /**
     * Adds a new menu item.
     */
    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<MenuItem> addMenuItem(@RequestBody @Valid MenuItem menuItem) {
        return ResponseUtils.handleRequest(() -> menuItemService.addMenuItem(menuItem));
    }

    /**
     * Updates an existing menu item by its ID.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody @Valid MenuItem menuItemDetails) {
        return ResponseUtils.handleRequest(() -> menuItemService.updateMenuItem(id, menuItemDetails));
    }

    /**
     * Deletes a menu item by its ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        return ResponseUtils.handleRequest(() -> {
            menuItemService.deleteMenuItem(id);
            return Map.of("message", "Menu item deleted successfully.");
        });
    }

    /**
     * Updates the inventory of a single menu item for a specific restaurant.
     */
    @PatchMapping("/{menuItemId}/inventory")
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<?> updateInventory(
            @PathVariable String slug,
            @PathVariable Long menuItemId,
            @RequestBody @Valid InventoryUpdateRequestDTO inventoryUpdate) {
        return ResponseUtils.handleRequest(() ->
                menuItemService.updateMenuItemInventory(slug, menuItemId, inventoryUpdate));
    }

    /**
     * Uploads a CSV file to update the menu items.
     * Only accessible to users with the role 'RESTAURANT_EMPLOYEE'.
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<?> uploadMenu(@RequestParam("file") MultipartFile file) {
        return ResponseUtils.handleRequest(() -> menuItemService.handleCsvUpload(file));
    }
}
