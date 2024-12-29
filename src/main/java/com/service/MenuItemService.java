package com.service;

import com.model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.repository.MenuItemRepository;
import com.exception.ResourceNotFoundException;


import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

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
    public MenuItem updateMenuItem(final Long id, final MenuItem menuItemDetails) {
        final MenuItem menuItem = this.menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        // Update the fields using the setters
        menuItem.setName(menuItemDetails.getName());
        menuItem.setDescription(menuItemDetails.getDescription());
        menuItem.setPrice(menuItemDetails.getPrice());
        menuItem.setIngredients(menuItemDetails.getIngredients());

        return this.menuItemRepository.save(menuItem);
    }

    public void deleteMenuItem(final Long id) {
        this.menuItemRepository.deleteById(id);
    }
}
