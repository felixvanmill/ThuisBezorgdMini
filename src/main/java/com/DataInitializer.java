package com;

import com.model.AppUser;
import com.model.Address;
import com.model.CustomerOrder;
import com.model.MenuItem;
import com.model.Restaurant;
import com.repository.AppUserRepository;
import com.repository.AddressRepository;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create and save users
        AppUser user1 = new AppUser("johndoe", "password123", "CUSTOMER", "John Doe");
        AppUser user2 = new AppUser("marysmith", "password123", "RESTAURANT_EMPLOYEE", "Mary Smith");
        AppUser user3 = new AppUser("alexjohnson", "password123", "DELIVERY_PERSON", "Alex Johnson");

        appUserRepository.saveAll(Arrays.asList(user1, user2, user3));

        // Create and save restaurants
        Restaurant restaurant1 = new Restaurant("Pizza Place", "Pizzeria specialized in Italian dishes", "123 Main St, City");
        Restaurant restaurant2 = new Restaurant("Sushi World", "Authentic Japanese restaurant with fresh sushi", "456 Ocean Ave, City");
        restaurantRepository.saveAll(Arrays.asList(restaurant1, restaurant2));

        // Create and save menu items
        MenuItem pizza1 = new MenuItem("Margherita Pizza", "Classic cheese and tomato", 9.99, "Cheese, Tomato, Basil", restaurant1);
        MenuItem pizza2 = new MenuItem("Pepperoni Pizza", "Cheese, tomato, and pepperoni", 11.99, "Cheese, Tomato, Pepperoni", restaurant1);
        MenuItem sushi1 = new MenuItem("California Roll", "Crab, avocado, and cucumber", 8.99, "Crab, Avocado, Cucumber", restaurant2);
        MenuItem sushi2 = new MenuItem("Spicy Tuna Roll", "Tuna with spicy sauce", 10.99, "Tuna, Spicy Mayo", restaurant2);
        menuItemRepository.saveAll(Arrays.asList(pizza1, pizza2, sushi1, sushi2));

        // Create addresses (no need to manually save as cascade will handle it)
        Address address1 = new Address("Customer Lane", "123", "12345", "City");
        Address address2 = new Address("Another St", "456", "67890", "City");

        // Create orders, associating them with the previously created addresses
        CustomerOrder order1 = new CustomerOrder(user1, Arrays.asList(pizza1, pizza2), address1, "PENDING", 21.98);
        CustomerOrder order2 = new CustomerOrder(user1, Arrays.asList(sushi1, sushi2), address2, "DELIVERED", 19.98);

        // Save orders (addresses will be saved automatically due to cascading)
        customerOrderRepository.saveAll(Arrays.asList(order1, order2));

        // Log success message
        System.out.println("Sample data successfully added!");
    }
}
