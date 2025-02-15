// src/main/java/com/service/MenuItemService.java

package com.service;

import com.dto.InventoryUpdateRequestDTO;
import com.exception.ResourceNotFoundException;
import com.model.AppUser;
import com.model.MenuItem;
import com.repository.AppUserRepository;
import com.repository.MenuItemRepository;
import com.utils.CsvUtils;
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
    public void updateInventory(String slug, InventoryUpdateRequestDTO inventoryUpdate) {
        MenuItem menuItem = menuItemRepository.findById(inventoryUpdate.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with ID: " + inventoryUpdate.getMenuItemId()));

        if (!menuItem.getRestaurant().getSlug().equals(slug)) {
            throw new IllegalArgumentException("MenuItem does not belong to restaurant: " + slug);
        }

        menuItem.setInventory(inventoryUpdate.getQuantity());
        menuItemRepository.save(menuItem);
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

            // ✅ Controleer correct of de header "name" bestaat
            if (!parser.getHeaderMap().containsKey("name")) {
                errorMessages.add("CSV is missing the 'name' column.");
                return errorMessages; // **Fix: Stop verwerking en retourneer fout**
            }

            for (CSVRecord record : parser) {
                try {
                    String name = "";
                    if (record.isMapped("name")) {
                        name = record.get("name");
                        if (name == null) {
                            name = "";
                        }
                    }

                    if (name.isBlank()) {
                        errorMessages.add("Row " + record.getRecordNumber() + ": Missing or empty 'name' field.");
                        continue;
                    }



                    String description = record.isMapped("description") ? record.get("description") : "No description provided";
                    String priceStr = record.isMapped("price") ? record.get("price") : "0.0";
                    String inventoryStr = record.isMapped("inventory") ? record.get("inventory") : "0";

                    double price = !priceStr.isBlank() ? Double.parseDouble(priceStr) : 0.0;
                    int inventory = !inventoryStr.isBlank() ? Integer.parseInt(inventoryStr) : 0;

                    String itemId = record.isMapped("id") ? record.get("id") : null;

                    MenuItem menuItem;
                    if (itemId != null && !itemId.isBlank()) {
                        menuItem = menuItemRepository.findById(Long.parseLong(itemId)).orElse(new MenuItem());
                    } else {
                        menuItem = new MenuItem();
                    }

                    menuItem.setName(name);
                    menuItem.setDescription(description);
                    menuItem.setPrice(price);
                    menuItem.setInventory(inventory);

                    menuItemRepository.save(menuItem);
                } catch (Exception e) {
                    System.out.println("DEBUG ERROR: " + e.getMessage());
                    errorMessages.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("DEBUG FILE ERROR: " + e.getMessage());
            errorMessages.add("Failed to process file: " + e.getMessage());
        }

        return errorMessages;
    }

    @Transactional
    public List<String> processCsvFile(MultipartFile file, Long restaurantId) {
        return CsvUtils.processMenuCsvFile(file, restaurantId, this);
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
