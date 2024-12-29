package com.controller;

import com.model.CustomerOrder;
import com.model.MenuItem;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/restaurant")
public class RestaurantController { private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    // ✅ Restaurant management page by slug
    @GetMapping("/{slug}/management")
    public String restaurantManagementBySlug(@PathVariable final String slug, final Model model) {
        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        final List<CustomerOrder> orders = this.customerOrderRepository.findByRestaurant_Id(restaurant.getId());
        final List<MenuItem> menuItems = this.menuItemRepository.findByRestaurant_Id(restaurant.getId());

        model.addAttribute("username", this.getLoggedInUsername());
        model.addAttribute("welcomeMessage", "Welcome to your restaurant management dashboard!");
        model.addAttribute("orders", orders);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantName", restaurant.getName());
        model.addAttribute("slug", slug);

        return "restaurant/restaurant"; // Maps to templates/restaurant/restaurant.html
    }

    // ✅ Default restaurant management page (redirect to slug-based URL)
    @GetMapping("/management")
    public String restaurantManagementRedirect() {
        final String username = this.getLoggedInUsername();

        final Restaurant restaurant = this.restaurantRepository.findByEmployees_Username(username)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for user: " + username));

        return "redirect:/restaurant/" + restaurant.getSlug() + "/management";
    }

    // ✅ View menu for a restaurant (with slug)
    @GetMapping("/{slug}/menu")
    public String viewMenuBySlug(@PathVariable final String slug, final Model model) {
        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));

        final List<MenuItem> menuItems = this.menuItemRepository.findByRestaurant_Id(restaurant.getId());

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantName", restaurant.getName());
        return "customer/menu";
    }

    // ✅ View order details by slug and order number
    @GetMapping("/{slug}/orders/{orderNumber}/details")
    public String viewOrderDetailsByOrderNumber(
            @PathVariable final String slug,
            @PathVariable final String orderNumber,
            final Model model) {

        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        final CustomerOrder order = this.customerOrderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));

        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new RuntimeException("Order does not belong to the specified restaurant.");
        }

        model.addAttribute("order", order);
        model.addAttribute("items", order.getOrderItems());
        return "restaurant/orderDetails";
    }

    // ✅ Update order status by slug and order ID and restricts non-allowed changes
    @PostMapping("/{slug}/orders/{orderId}/updateStatus")
    public String updateOrderStatus(
            @PathVariable final String slug,
            @PathVariable final Long orderId,
            @RequestParam final String status) {

        // Fetch the restaurant
        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        // Fetch the order
        final CustomerOrder order = this.customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Verify the order belongs to the restaurant
        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new RuntimeException("Order does not belong to the specified restaurant.");
        }

        try {
            final OrderStatus orderStatus = OrderStatus.valueOf(status); // ✅ Convert String to Enum

            // ✅ Restrict statuses allowed for restaurant employees
            if (!List.of(
                    OrderStatus.UNCONFIRMED,
                    OrderStatus.IN_KITCHEN,
                    OrderStatus.READY_FOR_DELIVERY
            ).contains(orderStatus)) {
                throw new IllegalArgumentException("Unauthorized status change: " + status);
            }

            order.setStatus(orderStatus);
            this.customerOrderRepository.save(order);
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("Invalid or unauthorized status value: " + status);
        }

        return "redirect:/restaurant/" + slug + "/management";
    }


    // ✅ Serve file upload form by slug
    @GetMapping("/{slug}/menu/upload")
    public String uploadMenuForm(@PathVariable final String slug, final Model model) {
        model.addAttribute("message", "Upload a CSV file to add new menu items.");
        model.addAttribute("slug", slug);
        return "restaurant/uploadMenu";
    }

    // ✅ Handle CSV upload by slug
    @PostMapping("/{slug}/menu/upload")
    public String uploadMenuItems(
            @PathVariable final String slug,
            @RequestParam("file") final MultipartFile file,
            final Model model) {
        final Restaurant restaurant = this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        final List<MenuItem> menuItems = new ArrayList<>();
        try (final Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             final CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (final CSVRecord record : csvParser) {
                final String name = record.get("name").trim();
                final String description = record.get("description").trim();
                final double price = Double.parseDouble(record.get("price").trim());
                final String ingredients = record.get("ingredients").trim().replace("|", ", ");
                final int inventory = Integer.parseInt(record.get("inventory").trim());

                final MenuItem menuItem = new MenuItem(name, description, price, ingredients, restaurant, inventory);
                menuItems.add(menuItem);
            }

            this.menuItemRepository.saveAll(menuItems);
            model.addAttribute("success", "Menu items uploaded successfully!");
        } catch (final Exception e) {
            model.addAttribute("error", "Error processing CSV file: " + e.getMessage());
        }

        return "restaurant/uploadMenu";
    }

    // ✅ Validate restaurant by slug
    private Restaurant validateRestaurant(final String slug) {
        return this.restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));
    }

    // ✅ Validate menu item belongs to a restaurant
    private MenuItem validateMenuItem(final Long menuItemId, final Long restaurantId) {
        final MenuItem menuItem = this.menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("MenuItem not found with ID: " + menuItemId));
        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("MenuItem does not belong to the specified restaurant.");
        }
        return menuItem;
    }

    @PostMapping("/{slug}/menu/{menuItemId}/updateInventory")
    public String updateInventory(
            @PathVariable final String slug,
            @PathVariable final Long menuItemId,
            @RequestParam final int quantity,
            final Model model) {

        try {
            // Validate restaurant and menu item
            final Restaurant restaurant = this.validateRestaurant(slug);
            final MenuItem menuItem = this.validateMenuItem(menuItemId, restaurant.getId());

            // Validate inventory quantity
            if (0 > quantity) {
                throw new IllegalArgumentException("Inventory quantity cannot be negative.");
            }

            // Update inventory
            menuItem.setInventory(menuItem.getInventory() + quantity);
            this.menuItemRepository.save(menuItem);

            model.addAttribute("success", "Inventory updated successfully!");

        } catch (final IllegalArgumentException e) {
            RestaurantController.logger.error("Invalid inventory update attempt: ", e);
            model.addAttribute("error", "Invalid quantity: " + e.getMessage());
        } catch (final RuntimeException e) {
            RestaurantController.logger.error("Validation failed during inventory update: ", e);
            model.addAttribute("error", "Error: " + e.getMessage());
        } catch (final Exception e) {
            RestaurantController.logger.error("Unexpected error updating inventory: ", e);
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "redirect:/restaurant/" + slug + "/management";
    }



    private String getLoggedInUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
