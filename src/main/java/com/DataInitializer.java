package com;

import com.model.*;
import com.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create and save users with encrypted passwords
        AppUser user1 = new AppUser("johndoe", passwordEncoder.encode("password123"), "CUSTOMER", "John Doe");
        AppUser user2 = new AppUser("marysmith", passwordEncoder.encode("password123"), "RESTAURANT_EMPLOYEE", "Mary Smith");
        AppUser user3 = new AppUser("alexjohnson", passwordEncoder.encode("password123"), "DELIVERY_PERSON", "Alex Johnson");

        appUserRepository.saveAll(Arrays.asList(user1, user2, user3));

        // Create and save restaurants
        Restaurant restaurant1 = new Restaurant("Pizza Place", "Pizzeria specializing in Italian dishes", "123 Main St, City");
        Restaurant restaurant2 = new Restaurant("Sushi World", "Authentic Japanese restaurant with fresh sushi", "456 Ocean Ave, City");
        restaurantRepository.saveAll(Arrays.asList(restaurant1, restaurant2));

        // Create and save menu items
        MenuItem pizza1 = new MenuItem("Margherita Pizza", "Classic cheese and tomato", 9.99, "Cheese, Tomato, Basil", restaurant1);
        MenuItem pizza2 = new MenuItem("Pepperoni Pizza", "Cheese, tomato, and pepperoni", 11.99, "Cheese, Tomato, Pepperoni", restaurant1);
        MenuItem sushi1 = new MenuItem("California Roll", "Crab, avocado, and cucumber", 8.99, "Crab, Avocado, Cucumber", restaurant2);
        MenuItem sushi2 = new MenuItem("Spicy Tuna Roll", "Tuna with spicy sauce", 10.99, "Tuna, Spicy Mayo", restaurant2);
        menuItemRepository.saveAll(Arrays.asList(pizza1, pizza2, sushi1, sushi2));

        // Create addresses
        Address address1 = new Address("Customer Lane", "123", "12345", "City");
        Address address2 = new Address("Another St", "456", "67890", "City");

        // Create initial CustomerOrders with generated order numbers
        CustomerOrder order1 = new CustomerOrder(user1, null, address1, "PENDING", 0, restaurant1);
        CustomerOrder order2 = new CustomerOrder(user1, null, address2, "DELIVERED", 0, restaurant2);

        // Save orders initially to generate order numbers
        customerOrderRepository.saveAll(Arrays.asList(order1, order2));

        // Create OrderItems with order numbers
        OrderItem orderItem1 = new OrderItem(pizza1, 2, order1.getOrderNumber());  // 2 Margherita Pizzas
        OrderItem orderItem2 = new OrderItem(pizza2, 1, order1.getOrderNumber());  // 1 Pepperoni Pizza
        OrderItem orderItem3 = new OrderItem(sushi1, 3, order2.getOrderNumber());  // 3 California Rolls
        OrderItem orderItem4 = new OrderItem(sushi2, 2, order2.getOrderNumber());  // 2 Spicy Tuna Rolls

        // Attach OrderItems to CustomerOrders and calculate total prices
        order1.setOrderItems(List.of(orderItem1, orderItem2));
        order2.setOrderItems(List.of(orderItem3, orderItem4));

        // Save updated CustomerOrders with OrderItems and total prices
        customerOrderRepository.saveAll(Arrays.asList(order1, order2));

        // Log success message
        System.out.println("Sample data successfully added with encrypted passwords, restaurant associations, order numbers, and item quantities!");
    }
}
