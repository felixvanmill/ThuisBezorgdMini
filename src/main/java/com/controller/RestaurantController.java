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
import org.springframework.security.access.prepost.PreAuthorize;



@Controller
@RequestMapping("/restaurant")
public class RestaurantController {
    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    // Serve the restaurant management page for a specific restaurant (identified by slug).
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/management")
    public String restaurantManagementBySlug(@PathVariable String slug, Model model) {
        Restaurant restaurant = validateRestaurant(slug); // Ensure the restaurant exists.

        // Retrieve orders and menu items associated with the restaurant.
        List<CustomerOrder> orders = customerOrderRepository.findByRestaurant_Id(restaurant.getId());
        List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());

        // Pass data to the Thymeleaf template.
        model.addAttribute("username", getLoggedInUsername());
        model.addAttribute("welcomeMessage", "Welcome to your restaurant management dashboard!");
        model.addAttribute("orders", orders);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantName", restaurant.getName());
        model.addAttribute("slug", slug);

        return "restaurant/restaurant"; // Return the management dashboard template.
    }

    // Redirects to the slug-based restaurant management URL for the logged-in employee.
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/management")
    public String restaurantManagementRedirect() {
        String username = getLoggedInUsername();
        // Find the restaurant linked to the logged-in employee.
        Restaurant restaurant = restaurantRepository.findByEmployees_Username(username)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for user: " + username));

        return "redirect:/restaurant/" + restaurant.getSlug() + "/management";
    }

    // Serve the menu for a specific restaurant (identified by slug) to customers.
    @GetMapping("/{slug}/menu")
    public String viewMenuBySlug(@PathVariable String slug, Model model) {
        Restaurant restaurant = validateRestaurant(slug); // Ensure the restaurant exists.

        // Fetch the menu items for the restaurant.
        List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());

        // Pass menu data to the template.
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantName", restaurant.getName());
        return "customer/menu"; // Return the menu page template.
    }

    // View the details of a specific customer order (by order number) for the restaurant.
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/orders/{orderNumber}/details")
    public String viewOrderDetailsByOrderNumber(
            @PathVariable String slug,
            @PathVariable String orderNumber,
            Model model) {

        Restaurant restaurant = validateRestaurant(slug); // Ensure the restaurant exists.
        // Find the order by its order number.
        CustomerOrder order = customerOrderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));

        // Ensure the order belongs to the restaurant.
        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new RuntimeException("Order does not belong to the specified restaurant.");
        }

        // Pass order details to the template.
        model.addAttribute("order", order);
        model.addAttribute("items", order.getOrderItems());
        return "restaurant/orderDetails"; // Return the order details page.
    }

    // Update the status of a specific customer order.
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/{slug}/orders/{orderId}/updateStatus")
    public String updateOrderStatus(
            @PathVariable String slug,
            @PathVariable Long orderId,
            @RequestParam String status) {

        Restaurant restaurant = validateRestaurant(slug); // Ensure the restaurant exists.
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Ensure the order belongs to the restaurant.
        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new RuntimeException("Order does not belong to the specified restaurant.");
        }

        try {
            // Validate and update the order status.
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            if (!List.of(OrderStatus.UNCONFIRMED, OrderStatus.IN_KITCHEN, OrderStatus.READY_FOR_DELIVERY).contains(orderStatus)) {
                throw new IllegalArgumentException("Unauthorized status change: " + status);
            }
            order.setStatus(orderStatus);
            customerOrderRepository.save(order); // Persist the status change.
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid or unauthorized status value: " + status);
        }

        return "redirect:/restaurant/" + slug + "/management";
    }

    // Serve the file upload form to upload new menu items via CSV.
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/menu/upload")
    public String uploadMenuForm(@PathVariable String slug, Model model) {
        model.addAttribute("message", "Upload a CSV file to add new menu items.");
        model.addAttribute("slug", slug);
        return "restaurant/uploadMenu";
    }

    // Handle the uploaded CSV file to add new menu items.
    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/{slug}/menu/upload")
    public String uploadMenuItems(
            @PathVariable String slug,
            @RequestParam("file") MultipartFile file,
            Model model) {

        Restaurant restaurant = validateRestaurant(slug); // Ensure the restaurant exists.
        List<MenuItem> menuItems = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // Parse the CSV records and create menu items.
            for (CSVRecord record : csvParser) {
                MenuItem menuItem = new MenuItem(
                        record.get("name").trim(),
                        record.get("description").trim(),
                        Double.parseDouble(record.get("price").trim()),
                        record.get("ingredients").trim().replace("|", ", "),
                        restaurant,
                        Integer.parseInt(record.get("inventory").trim())
                );
                menuItems.add(menuItem);
            }

            menuItemRepository.saveAll(menuItems); // Save all menu items.
            model.addAttribute("success", "Menu items uploaded successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error processing CSV file: " + e.getMessage());
        }

        return "restaurant/uploadMenu";
    }

    // Utility method to ensure a restaurant exists by slug.
    private Restaurant validateRestaurant(String slug) {
        return restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));
    }

    // Utility method to fetch the username of the logged-in user.
    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
