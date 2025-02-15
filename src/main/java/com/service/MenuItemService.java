package com.service;

import com.dto.InventoryUpdateRequestDTO;
import com.exception.ResourceNotFoundException;
import com.exception.ValidationException;
import com.model.AppUser;
import com.model.MenuItem;
import com.repository.AppUserRepository;
import com.repository.MenuItemRepository;
import com.utils.AuthUtils;
import com.utils.CsvUtils;
import com.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final AppUserRepository appUserRepository;

    @Autowired
    public MenuItemService(MenuItemRepository menuItemRepository, AppUserRepository appUserRepository) {
        this.menuItemRepository = menuItemRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Get all menu items.
     */
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    /**
     * Get a menu item by ID.
     */
    public MenuItem getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + id));
    }

    /**
     * Add a new menu item.
     */
    public MenuItem addMenuItem(MenuItem menuItem) {
        validateMenuItem(menuItem);
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
            throw new ValidationException("MenuItem does not belong to restaurant: " + slug);
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

        validateMenuItem(menuItemDetails);

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
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu item not found with ID: " + id);
        }
        menuItemRepository.deleteById(id);
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (user.getRestaurant() == null) {
            throw new ValidationException("No restaurant is associated with the user: " + username);
        }

        return user.getRestaurant().getId();
    }

    @Transactional
    public Map<String, Object> updateMenuItemInventory(String slug, Long menuItemId, InventoryUpdateRequestDTO inventoryUpdate) {
        updateInventory(slug, inventoryUpdate);  // Existing logic moved here
        return Map.of(
                "message", "Inventory updated successfully.",
                "menuItemId", menuItemId,
                "newInventory", inventoryUpdate.getQuantity()
        );
    }

    @Transactional
    public Map<String, Object> handleCsvUpload(MultipartFile file) {
        FileUtils.validateCsvFile(file);
        String username = AuthUtils.getLoggedInUsername();
        Long restaurantId = getRestaurantIdForUser(username);
        List<String> errorMessages = processCsvFile(file, restaurantId);

        if (!errorMessages.isEmpty()) {
            return Map.of(
                    "message", "Menu update partially successful. Some records failed.",
                    "errors", errorMessages
            );
        }
        return Map.of("message", "Menu updated successfully!");
    }

    /**
     * Validates menu item details.
     */
    private void validateMenuItem(MenuItem menuItem) {
        if (menuItem.getName() == null || menuItem.getName().trim().isEmpty()) {
            throw new ValidationException("Menu item name cannot be empty.");
        }
        if (menuItem.getPrice() == null) { // ✅ This now works!
            throw new ValidationException("Menu item price cannot be null.");
        }
        if (menuItem.getPrice() <= 0) {
            throw new ValidationException("Menu item price must be greater than 0.");
        }
    }

}
