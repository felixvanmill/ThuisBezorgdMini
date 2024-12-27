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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    // ✅ Restaurant management page by slug
    @GetMapping("/{slug}/management")
    public String restaurantManagementBySlug(@PathVariable String slug, Model model) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        List<CustomerOrder> orders = customerOrderRepository.findByRestaurant_Id(restaurant.getId());
        List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());

        model.addAttribute("username", getLoggedInUsername());
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
        String username = getLoggedInUsername();

        Restaurant restaurant = restaurantRepository.findByEmployees_Username(username)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for user: " + username));

        return "redirect:/restaurant/" + restaurant.getSlug() + "/management";
    }

    // ✅ View menu for a restaurant (with slug)
    @GetMapping("/{slug}/menu")
    public String viewMenuBySlug(@PathVariable String slug, Model model) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with slug: " + slug));

        List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantName", restaurant.getName());
        return "customer/menu";
    }

    // ✅ View order details by slug and order number
    @GetMapping("/{slug}/orders/{orderNumber}/details")
    public String viewOrderDetailsByOrderNumber(
            @PathVariable String slug,
            @PathVariable String orderNumber,
            Model model) {

        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        CustomerOrder order = customerOrderRepository.findByOrderNumberWithItems(orderNumber)
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
            @PathVariable String slug,
            @PathVariable Long orderId,
            @RequestParam String status) {

        // Fetch the restaurant
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        // Fetch the order
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Verify the order belongs to the restaurant
        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new RuntimeException("Order does not belong to the specified restaurant.");
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status); // ✅ Convert String to Enum

            // ✅ Restrict statuses allowed for restaurant employees
            if (!List.of(
                    OrderStatus.UNCONFIRMED,
                    OrderStatus.IN_KITCHEN,
                    OrderStatus.READY_FOR_DELIVERY
            ).contains(orderStatus)) {
                throw new IllegalArgumentException("Unauthorized status change: " + status);
            }

            order.setStatus(orderStatus);
            customerOrderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid or unauthorized status value: " + status);
        }

        return "redirect:/restaurant/" + slug + "/management";
    }


    // ✅ Serve file upload form by slug
    @GetMapping("/{slug}/menu/upload")
    public String uploadMenuForm(@PathVariable String slug, Model model) {
        model.addAttribute("message", "Upload a CSV file to add new menu items.");
        model.addAttribute("slug", slug);
        return "restaurant/uploadMenu";
    }

    // ✅ Handle CSV upload by slug
    @PostMapping("/{slug}/menu/upload")
    public String uploadMenuItems(
            @PathVariable String slug,
            @RequestParam("file") MultipartFile file,
            Model model) {
        Restaurant restaurant = restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));

        List<MenuItem> menuItems = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                String name = record.get("name").trim();
                String description = record.get("description").trim();
                double price = Double.parseDouble(record.get("price").trim());
                String ingredients = record.get("ingredients").trim().replace("|", ", ");
                int inventory = Integer.parseInt(record.get("inventory").trim());

                MenuItem menuItem = new MenuItem(name, description, price, ingredients, restaurant, inventory);
                menuItems.add(menuItem);
            }

            menuItemRepository.saveAll(menuItems);
            model.addAttribute("success", "Menu items uploaded successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error processing CSV file: " + e.getMessage());
        }

        return "restaurant/uploadMenu";
    }

    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
