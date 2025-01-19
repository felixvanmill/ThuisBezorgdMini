package com.service;

import com.dto.RestaurantDTO;
import com.model.MenuItem;
import com.model.Restaurant;
import com.repository.MenuItemRepository;
import com.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant mockRestaurant;
    private List<MenuItem> mockMenuItems;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up a mock restaurant
        mockRestaurant = new Restaurant();
        mockRestaurant.setId(1L);
        mockRestaurant.setName("Pizza Place");
        mockRestaurant.setSlug("pizza-place");

        // Set up mock menu items
        MenuItem item1 = new MenuItem();
        item1.setId(1L);
        item1.setName("Margherita Pizza");
        item1.setPrice(9.99);
        item1.setAvailable(true);

        MenuItem item2 = new MenuItem();
        item2.setId(2L);
        item2.setName("Pepperoni Pizza");
        item2.setPrice(11.99);
        item2.setAvailable(true);

        mockMenuItems = List.of(item1, item2);
    }

    @Test
    void testGetRestaurantBySlug() {
        // Arrange
        when(restaurantRepository.findBySlug("pizza-place")).thenReturn(Optional.of(mockRestaurant));

        // Act
        Optional<Restaurant> result = restaurantService.getRestaurantBySlug("pizza-place");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockRestaurant.getName(), result.get().getName());
        verify(restaurantRepository, times(1)).findBySlug("pizza-place");
    }

    @Test
    void testGetRestaurantWithMenuIncludeInventory() {
        // Arrange
        when(restaurantRepository.findBySlug("pizza-place")).thenReturn(Optional.of(mockRestaurant));
        when(menuItemRepository.findByRestaurant_Id(mockRestaurant.getId())).thenReturn(mockMenuItems);

        // Act
        RestaurantDTO result = restaurantService.getRestaurantWithMenu("pizza-place", true);

        // Assert
        assertEquals(mockRestaurant.getName(), result.getName());
        assertEquals(2, result.getMenuItems().size());
        assertEquals(mockMenuItems.get(0).getName(), result.getMenuItems().get(0).getName());
        verify(restaurantRepository, times(1)).findBySlug("pizza-place");
        verify(menuItemRepository, times(1)).findByRestaurant_Id(mockRestaurant.getId());
    }

    @Test
    void testGetRestaurantWithMenuExcludeInventory() {
        // Arrange
        when(restaurantRepository.findBySlug("pizza-place")).thenReturn(Optional.of(mockRestaurant));
        when(menuItemRepository.findByRestaurant_IdAndIsAvailable(mockRestaurant.getId(), true)).thenReturn(mockMenuItems);

        // Act
        RestaurantDTO result = restaurantService.getRestaurantWithMenu("pizza-place", false);

        // Assert
        assertEquals(mockRestaurant.getName(), result.getName());
        assertEquals(2, result.getMenuItems().size());
        verify(restaurantRepository, times(1)).findBySlug("pizza-place");
        verify(menuItemRepository, times(1)).findByRestaurant_IdAndIsAvailable(mockRestaurant.getId(), true);
    }

    @Test
    void testGetRestaurantBySlugNotFound() {
        // Arrange
        when(restaurantRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

        // Act
        Optional<Restaurant> result = restaurantService.getRestaurantBySlug("non-existent");

        // Assert
        assertFalse(result.isPresent());
        verify(restaurantRepository, times(1)).findBySlug("non-existent");
    }

    @Test
    void testAddRestaurant() {
        // Arrange
        when(restaurantRepository.save(mockRestaurant)).thenReturn(mockRestaurant);

        // Act
        Restaurant result = restaurantService.addRestaurant(mockRestaurant);

        // Assert
        assertNotNull(result);
        assertEquals(mockRestaurant.getName(), result.getName());
        verify(restaurantRepository, times(1)).save(mockRestaurant);
    }

    @Test
    void testDeleteRestaurant() {
        // Arrange
        Long restaurantId = mockRestaurant.getId();
        doNothing().when(restaurantRepository).deleteById(restaurantId);

        // Act
        restaurantService.deleteRestaurant(restaurantId);

        // Assert
        verify(restaurantRepository, times(1)).deleteById(restaurantId);
    }
}
