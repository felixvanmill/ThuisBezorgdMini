// src/main/java/com/service/MenuItemService.java

package com.service;

import com.dto.InventoryUpdateRequest;
import com.exception.ResourceNotFoundException;
import com.model.AppUser;
import com.model.MenuItem;
import com.repository.AppUserRepository;
import com.repository.MenuItemRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    /**
     * Get all menu items.
     */
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    /**
     * Get a menu item by ID.
     */
    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    /**
     * Add a new menu item.
     */
    public MenuItem addMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    /**
     * Update inventory for menu items in a specific restaurant.
     */
    @Transactional
    public void updateInventory(String slug, List<InventoryUpdateRequest> inventoryUpdates) {
        for (InventoryUpdateRequest request : inventoryUpdates) {
            MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with ID: " + request.getMenuItemId()));

            if (!menuItem.getRestaurant().getSlug().equals(slug)) {
                throw new IllegalArgumentException("MenuItem does not belong to the restaurant with slug: " + slug);
            }

            menuItem.setInventory(menuItem.getInventory() + request.getQuantity());
            if (menuItem.getInventory() < 0) {
                throw new IllegalArgumentException("Inventory cannot be negative for MenuItem ID: " + menuItem.getId());
            }

            menuItemRepository.save(menuItem);
        }
    }

    /**
     * Update a menu item.
     */
    @Transactional
    public MenuItem updateMenuItem(Long id, MenuItem menuItemDetails) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        menuItem.setName(menuItemDetails.getName());
        menuItem.setDescription(menuItemDetails.getDescription());
        menuItem.setPrice(menuItemDetails.getPrice());
        menuItem.setIngredients(menuItemDetails.getIngredients());
        menuItem.setInventory(menuItemDetails.getInventory());

        return menuItemRepository.save(menuItem);
    }

    /**
     * Delete a menu item by ID.
     */
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    /**
     * Process and import menu items from a CSV file.
     */
    public List<String> processMenuCSV(MultipartFile file) {
        List<String> errorMessages = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser parser = new CSVParser(reader,
                    CSVFormat.DEFAULT.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .build());

            for (CSVRecord record : parser) {
                try {
                    String name = record.get("name");
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("Missing or empty 'name' field.");
                    }

                    String description = record.get("description") != null ? record.get("description") : "No description provided";
                    double price = record.get("price") != null ? Double.parseDouble(record.get("price")) : 0.0;
                    int inventory = record.get("inventory") != null ? Integer.parseInt(record.get("inventory")) : 0;
                    String itemId = record.get("id");

                    MenuItem menuItem = itemId != null
                            ? menuItemRepository.findById(Long.parseLong(itemId)).orElse(new MenuItem())
                            : new MenuItem();

                    menuItem.setName(name);
                    menuItem.setDescription(description);
                    menuItem.setPrice(price);
                    menuItem.setInventory(inventory);

                    menuItemRepository.save(menuItem);
                } catch (Exception e) {
                    errorMessages.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errorMessages.add("Failed to process file: " + e.getMessage());
        }

        return errorMessages;
    }

    /**
     * Get the restaurant ID for a user by username.
     */
    public Long getRestaurantIdForUser(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        if (user.getRestaurant() == null) {
            throw new RuntimeException("No restaurant is associated with the user: " + username);
        }

        return user.getRestaurant().getId();
    }
}
