package service;

import model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.MenuItemRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    public MenuItem addMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    public MenuItem updateMenuItem(Long id, MenuItem menuItemDetails) {
        Optional<MenuItem> menuItemOptional = menuItemRepository.findById(id);
        if (menuItemOptional.isPresent()) {
            MenuItem menuItem = menuItemOptional.get();
            menuItem.setName(menuItemDetails.getName());
            menuItem.setDescription(menuItemDetails.getDescription());
            menuItem.setPrice(menuItemDetails.getPrice());
            menuItem.setIngredients(menuItemDetails.getIngredients());
            return menuItemRepository.save(menuItem);
        }
        return null; // Hier kun je eventueel een exception gooien als item niet gevonden wordt.
    }

    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }
}
