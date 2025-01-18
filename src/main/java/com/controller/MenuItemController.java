package com.controller;

import com.model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.service.MenuItemService;

import java.util.List;
import java.util.Optional;
import com.dto.InventoryUpdateRequest;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;





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

    @PostMapping("/upload")
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    public ResponseEntity<?> uploadMenu(@RequestParam("file") MultipartFile file) {
        try {
            // Validate the file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "The uploaded file is empty."));
            }
            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file format. Please upload a .csv file."));
            }
            if (file.getSize() > 2 * 1024 * 1024) { // Limit file size to 2MB
                return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds the 2MB limit."));
            }

            // Process the CSV
            List<String> errorMessages = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                CSVParser parser = new CSVParser(reader,
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .build());

                for (CSVRecord record : parser) {
                    try {
                        // Validate and parse each record
                        if (!record.isSet("name") || record.get("name").isBlank()) {
                            throw new IllegalArgumentException("Missing or empty 'name' field.");
                        }

                        String name = record.get("name");
                        String description = record.isSet("description") ? record.get("description") : "No description provided";
                        double price = record.isSet("price") ? Double.parseDouble(record.get("price")) : 0.0;
                        int inventory = record.isSet("inventory") ? Integer.parseInt(record.get("inventory")) : 0;
                        String itemId = record.isSet("id") ? record.get("id") : null;

                        // Create or update menu item
                        MenuItem menuItem = (itemId != null) ?
                                menuItemService.getMenuItemById(Long.parseLong(itemId)).orElse(new MenuItem()) :
                                new MenuItem();

                        menuItem.setName(name);
                        menuItem.setDescription(description);
                        menuItem.setPrice(price);
                        menuItem.setInventory(inventory);

                        // Save to the database
                        menuItemService.addMenuItem(menuItem);

                    } catch (Exception e) {
                        // Capture record-level errors
                        errorMessages.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                // Handle errors during file parsing
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse CSV file: " + e.getMessage()));
            }

            // If any rows failed, return detailed error messages
            if (!errorMessages.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Menu update partially successful. Some records failed.",
                        "errors", errorMessages
                ));
            }

            // Success response
            return ResponseEntity.ok(Map.of("message", "Menu updated successfully!"));

        } catch (Exception e) {
            // Catch-all for unexpected errors
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

}
