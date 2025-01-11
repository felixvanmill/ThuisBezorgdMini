package com.controller;

import com.dto.RestaurantDTO;
import com.model.CustomerOrder;
import com.model.MenuItem;
import com.model.OrderStatus;
import com.model.Restaurant;
import com.service.RestaurantService;
import com.repository.CustomerOrderRepository;
import com.repository.MenuItemRepository;
import com.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RestaurantService restaurantService;

    private static final String RESTAURANT_MANAGEMENT_TEMPLATE = "restaurant/restaurant";
    private static final String UPLOAD_MENU_TEMPLATE = "restaurant/uploadMenu";

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/{slug}/management")
    public String restaurantManagementBySlug(@PathVariable String slug, Model model) {
        Restaurant restaurant = validateRestaurant(slug);
        List<CustomerOrder> orders = customerOrderRepository.findByRestaurant_Id(restaurant.getId());
        List<MenuItem> menuItems = menuItemRepository.findByRestaurant_Id(restaurant.getId());

        model.addAttribute("username", getLoggedInUsername());
        model.addAttribute("welcomeMessage", "Welcome to your restaurant management dashboard!");
        model.addAttribute("orders", orders);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("restaurantName", restaurant.getName());
        model.addAttribute("slug", slug);

        return RESTAURANT_MANAGEMENT_TEMPLATE;
    }

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @GetMapping("/management")
    public String restaurantManagementRedirect() {
        String username = getLoggedInUsername();
        Restaurant restaurant = restaurantRepository.findByEmployees_Username(username)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for user: " + username));
        return "redirect:/restaurant/" + restaurant.getSlug() + "/management";
    }

    @GetMapping("/{slug}/menu")
    public String viewMenuBySlug(@PathVariable String slug, Model model) {
        RestaurantDTO restaurantDTO = restaurantService.getRestaurantWithMenu(slug);
        model.addAttribute("menuItems", restaurantDTO.getMenuItems());
        model.addAttribute("restaurantName", restaurantDTO.getName());
        return "customer/menu";
    }

    @PreAuthorize("hasRole('RESTAURANT_EMPLOYEE')")
    @PostMapping("/{slug}/orders/{orderId}/updateStatus")
    public String updateOrderStatus(@PathVariable String slug, @PathVariable Long orderId, @RequestParam String status) {
        Restaurant restaurant = validateRestaurant(slug);
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        if (!order.getRestaurant().getId().equals(restaurant.getId())) {
            throw new RuntimeException("Order does not belong to the specified restaurant.");
        }
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            order.setStatus(orderStatus);
            customerOrderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + status);
        }
        return "redirect:/restaurant/" + slug + "/management";
    }

    private Restaurant validateRestaurant(String slug) {
        return restaurantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Restaurant not found for slug: " + slug));
    }

    private String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
