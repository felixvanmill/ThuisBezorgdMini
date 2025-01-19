package com.controller;

import com.dto.InventoryUpdateRequest;
import com.model.MenuItem;
import com.service.MenuItemService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles menu item operations, including CRUD actions, inventory updates, and CSV uploads.
 */
@RestController
@RequestMapping("/restaurant/menu-items")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    /**
     * Fetches all menu items.
     */
    @GetMapping
    public List<MenuItem> getAllMenuItems() {
        return menuItemService.getAllMenuItems();
    }

    /**
     * Fetches a specific menu item by its ID.
     */
    @GetMapping("/{id}")
    public Optional<MenuItem> getMenuItemById(@PathVariable Long id) {
        return menuItemService.getMenuItemById(id);
    }

    /**
     * Adds a new menu item.
     */
    @PostMapping
    public MenuItem addMenuItem(@RequestBody MenuItem menuItem) {
        return menuItemService.addMenuItem(menuItem);
    }

    /**
     * Updates an existing menu item by its ID.
     */
    @PutMapping("/{id}")
    public MenuItem updateMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItemDetails) {
        return menuItemService.updateMenuItem(id, menuItemDetails);
    }

    /**
     * Deletes a menu item by its ID.
     */
    @DeleteMapping("/{id}")
    public void deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
    }

    /**
     * Updates the inventory of menu items for a specific restaurant.
     */
    @PostMapping("/{slug}/menu/inventory")
    public ResponseEntity<?> updateInventory(
            @PathVariable String slug,
            @RequestBody List<InventoryUpdateRequest> inventoryUpdates) {
        try {
            menuItemService.updateInventory(slug, inventoryUpdates);
            return ResponseEntity.ok(Map.of("message", "Inventory updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Uploads a CSV file to update the menu items.
     * Only accessible to users with the role 'RESTAURANT_EMPLOYEE'.
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<?> uploadMenu(@RequestParam("file") MultipartFile file) {
        try {
            validateFile(file);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Long restaurantId = menuItemService.getRestaurantIdForUser(username);

            List<String> errorMessages = processCsvFile(file, restaurantId);

            if (!errorMessages.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Menu update partially successful. Some records failed.",
                        "errors", errorMessages
                ));
            }

            return ResponseEntity.ok(Map.of("message", "Menu updated successfully!"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Validates the uploaded file.
     *
     * @param file The file to validate.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty.");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file format. Please upload a .csv file.");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds the 2MB limit.");
        }
    }

    /**
     * Processes the uploaded CSV file and updates the menu items.
     *
     * @param file         The CSV file to process.
     * @param restaurantId The restaurant ID to associate with the menu items.
     * @return A list of error messages encountered during processing.
     */
    private List<String> processCsvFile(MultipartFile file, Long restaurantId) {
        List<String> errorMessages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : parser) {
                try {
                    String name = record.get("name");
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("Missing or empty 'name' field.");
                    }

                    String description = record.isSet("description") ? record.get("description") : "No description provided";
                    double price = record.isSet("price") ? Double.parseDouble(record.get("price")) : 0.0;
                    int inventory = record.isSet("inventory") ? Integer.parseInt(record.get("inventory")) : 0;
                    String ingredients = record.isSet("ingredients") ? record.get("ingredients") : null;
                    String itemId = record.isSet("id") ? record.get("id") : null;

                    MenuItem menuItem = (itemId != null) ?
                            menuItemService.getMenuItemById(Long.parseLong(itemId)).orElse(new MenuItem()) :
                            new MenuItem();

                    menuItem.setName(name);
                    menuItem.setDescription(description);
                    menuItem.setPrice(price);
                    menuItem.setInventory(inventory);
                    menuItem.setIngredients(ingredients);
                    menuItem.setRestaurantId(restaurantId);

                    menuItemService.addMenuItem(menuItem);

                } catch (Exception e) {
                    errorMessages.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errorMessages.add("Failed to process the file: " + e.getMessage());
        }
        return errorMessages;
    }
}
