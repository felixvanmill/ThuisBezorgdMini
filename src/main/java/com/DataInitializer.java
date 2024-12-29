package com;

import com.model.*;
import com.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    public void run(final String... args) throws Exception {
        // ✅ Create and save users with encrypted passwords
        final AppUser user1 = new AppUser("johndoe", this.passwordEncoder.encode("password123"), "CUSTOMER", "John Doe");
        final AppUser restaurantEmployee1 = new AppUser("marysmith", this.passwordEncoder.encode("password123"), "RESTAURANT_EMPLOYEE", "Mary Smith");
        final AppUser restaurantEmployee2 = new AppUser("pizzachef", this.passwordEncoder.encode("password123"), "RESTAURANT_EMPLOYEE", "Pizza Chef");
        final AppUser deliveryPerson = new AppUser("alexjohnson", this.passwordEncoder.encode("password123"), "DELIVERY_PERSON", "Alex Johnson");

        this.appUserRepository.saveAll(Arrays.asList(user1, restaurantEmployee1, restaurantEmployee2, deliveryPerson));

        // ✅ Create and save restaurants
        final Restaurant restaurant1 = new Restaurant("Pizza Place", "Pizzeria specializing in Italian dishes", "123 Main St, City");
        final Restaurant restaurant2 = new Restaurant("Sushi World", "Authentic Japanese restaurant with fresh sushi", "456 Ocean Ave, City");
        this.restaurantRepository.saveAll(Arrays.asList(restaurant1, restaurant2));

        // ✅ Assign employees to restaurants
        restaurantEmployee1.setRestaurant(restaurant1);
        restaurantEmployee2.setRestaurant(restaurant2);
        this.appUserRepository.saveAll(Arrays.asList(restaurantEmployee1, restaurantEmployee2));

        // ✅ Create and save menu items
        final MenuItem pizza1 = new MenuItem("Margherita Pizza", "Classic cheese and tomato", 9.99, "Cheese, Tomato, Basil", restaurant1, 999);
        final MenuItem pizza2 = new MenuItem("Pepperoni Pizza", "Cheese, tomato, and pepperoni", 11.99, "Cheese, Tomato, Pepperoni", restaurant1, 999);
        final MenuItem sushi1 = new MenuItem("California Roll", "Crab, avocado, and cucumber", 8.99, "Crab, Avocado, Cucumber", restaurant2, 999);
        final MenuItem sushi2 = new MenuItem("Spicy Tuna Roll", "Tuna with spicy sauce", 10.99, "Tuna, Spicy Mayo", restaurant2, 999);
        this.menuItemRepository.saveAll(Arrays.asList(pizza1, pizza2, sushi1, sushi2));

        // ✅ Create addresses
        final Address address1 = new Address("Customer Lane", "123", "12345", "City");
        final Address address2 = new Address("Another St", "456", "67890", "City");

        // ✅ Create CustomerOrders with OrderStatus ENUM
        final CustomerOrder order1 = new CustomerOrder(
                user1,
                new ArrayList<>(),
                address1,
                OrderStatus.READY_FOR_DELIVERY,
                0,
                restaurant1
        );

        final CustomerOrder order2 = new CustomerOrder(
                user1,
                new ArrayList<>(),
                address2,
                OrderStatus.DELIVERED,
                0,
                restaurant2
        );

        // ✅ Save orders initially to generate order numbers
        this.customerOrderRepository.saveAll(Arrays.asList(order1, order2));

        // ✅ Create OrderItems and associate with orders
        final OrderItem orderItem1 = new OrderItem(pizza1, 2, order1.getOrderNumber());
        final OrderItem orderItem2 = new OrderItem(pizza2, 1, order1.getOrderNumber());
        final OrderItem orderItem3 = new OrderItem(sushi1, 3, order2.getOrderNumber());
        final OrderItem orderItem4 = new OrderItem(sushi2, 2, order2.getOrderNumber());

        // ✅ Attach OrderItems to CustomerOrders
        order1.setOrderItems(List.of(orderItem1, orderItem2));
        order2.setOrderItems(List.of(orderItem3, orderItem4));

        // ✅ Reduce inventory based on order quantities
        pizza1.setInventory(pizza1.getInventory() - orderItem1.getQuantity());
        pizza2.setInventory(pizza2.getInventory() - orderItem2.getQuantity());
        sushi1.setInventory(sushi1.getInventory() - orderItem3.getQuantity());
        sushi2.setInventory(sushi2.getInventory() - orderItem4.getQuantity());

        // ✅ Save updated CustomerOrders and MenuItems
        this.customerOrderRepository.saveAll(Arrays.asList(order1, order2));
        this.menuItemRepository.saveAll(Arrays.asList(pizza1, pizza2, sushi1, sushi2));

        // ✅ Log success message
        System.out.println("Sample data successfully added with encrypted passwords, restaurant associations, order numbers, inventory, slugs, and item quantities!");
    }
}
