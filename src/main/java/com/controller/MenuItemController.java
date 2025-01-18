package com.controller;

import com.model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.service.MenuItemService;

import java.util.List;
import java.util.Optional;
import com.dto.InventoryUpdateRequest;
import org.springframework.http.ResponseEntity;
import java.util.Map;



@RestController
@RequestMapping("/restaurant/menu-items")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    @GetMapping
    public List<MenuItem> getAllMenuItems() {
        return this.menuItemService.getAllMenuItems();
    }

    @GetMapping("/{id}")
    public Optional<MenuItem> getMenuItemById(@PathVariable final Long id) {
        return this.menuItemService.getMenuItemById(id);
    }

    @PostMapping
    public MenuItem addMenuItem(@RequestBody final MenuItem menuItem) {
        return this.menuItemService.addMenuItem(menuItem);
    }

    @PutMapping("/{id}")
    public MenuItem updateMenuItem(@PathVariable final Long id, @RequestBody final MenuItem menuItemDetails) {
        return this.menuItemService.updateMenuItem(id, menuItemDetails);
    }


    @DeleteMapping("/{id}")
    public void deleteMenuItem(@PathVariable final Long id) {
        this.menuItemService.deleteMenuItem(id);
    }

    @PostMapping("/{slug}/menu/inventory")
    public ResponseEntity<?> updateInventory(
            @PathVariable String slug,
            @RequestBody List<InventoryUpdateRequest> inventoryUpdates) {
        System.out.println("Received inventory update request for slug: " + slug);
        try {
            menuItemService.updateInventory(slug, inventoryUpdates);
            return ResponseEntity.ok(Map.of("message", "Inventory updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
