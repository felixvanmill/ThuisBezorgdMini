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
}
