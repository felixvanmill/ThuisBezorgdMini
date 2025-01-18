package com.service;

import com.model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.repository.MenuItemRepository;
import com.exception.ResourceNotFoundException;


import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import com.dto.InventoryUpdateRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList; // For List implementation
import java.io.BufferedReader; // For reading the CSV file
import java.io.InputStreamReader; // For InputStream processing
import org.apache.commons.csv.CSVParser; // For parsing CSV files
import org.apache.commons.csv.CSVRecord; // For individual records
import org.apache.commons.csv.CSVFormat; // For CSV file format




@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<MenuItem> getAllMenuItems() {
        return this.menuItemRepository.findAll();
    }

    public Optional<MenuItem> getMenuItemById(final Long id) {
        return this.menuItemRepository.findById(id);
    }

    public MenuItem addMenuItem(final MenuItem menuItem) {
        return this.menuItemRepository.save(menuItem);
    }

    @Transactional
    public void updateInventory(String slug, List<InventoryUpdateRequest> inventoryUpdates) {
        for (InventoryUpdateRequest request : inventoryUpdates) {
            MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with ID: " + request.getMenuItemId()));

            // Verify the menu item belongs to the restaurant with the given slug
            if (!menuItem.getRestaurant().getSlug().equals(slug)) {
                throw new IllegalArgumentException("MenuItem does not belong to the restaurant with slug: " + slug);
            }

            // Update inventory
            menuItem.setInventory(menuItem.getInventory() + request.getQuantity());

            if (menuItem.getInventory() < 0) {
                throw new IllegalArgumentException("Inventory cannot be negative for MenuItem ID: " + menuItem.getId());
            }

            menuItemRepository.save(menuItem);
        }
    }

    @Transactional
    public MenuItem updateMenuItem(final Long id, final MenuItem menuItemDetails) {
        final MenuItem menuItem = this.menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        // Update the fields using the setters
        menuItem.setName(menuItemDetails.getName());
        menuItem.setDescription(menuItemDetails.getDescription());
        menuItem.setPrice(menuItemDetails.getPrice());
        menuItem.setIngredients(menuItemDetails.getIngredients());
        menuItem.setInventory(menuItemDetails.getInventory());

        return this.menuItemRepository.save(menuItem);
    }

    public void deleteMenuItem(final Long id) {
        this.menuItemRepository.deleteById(id);
    }

    public List<String> processMenuCSV(MultipartFile file) {
        List<String> errorMessages = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser parser = new CSVParser(reader,
                    CSVFormat.DEFAULT.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true) // Skip header row
                            .build());

            for (CSVRecord record : parser) {
                try {
                    // Validate and parse fields
                    if (!record.isSet("name") || record.get("name").isBlank()) {
                        throw new IllegalArgumentException("Missing or empty 'name' field.");
                    }
                    String name = record.get("name");

                    String description = record.isSet("description") ? record.get("description") : "No description provided";
                    double price = record.isSet("price") ? Double.parseDouble(record.get("price")) : 0.0;
                    int inventory = record.isSet("inventory") ? Integer.parseInt(record.get("inventory")) : 0;

                    // Create or update MenuItem
                    String itemId = record.isSet("id") ? record.get("id") : null;
                    MenuItem menuItem = (itemId != null) ?
                            menuItemRepository.findById(Long.parseLong(itemId)).orElse(new MenuItem()) :
                            new MenuItem();

                    menuItem.setName(name);
                    menuItem.setDescription(description);
                    menuItem.setPrice(price);
                    menuItem.setInventory(inventory);

                    // Save MenuItem to the database
                    menuItemRepository.save(menuItem);

                } catch (Exception e) {
                    // Add detailed error for the current record
                    errorMessages.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errorMessages.add("Failed to process file: " + e.getMessage());
        }

        return errorMessages;
    }


}
