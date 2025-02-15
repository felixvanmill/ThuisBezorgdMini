package com.utils;

import com.dto.OrderDTO;
import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
import com.service.MenuItemService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;
import com.model.MenuItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CsvUtils {

    public static String generateCsvFromDTO(List<OrderDTO> orders, CustomerOrderRepository customerOrderRepository) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Order Number,Total Price,Status,Customer,Items\n");

        for (OrderDTO order : orders) {
            // Fetch items for each order (same as in the controller)
            String items = customerOrderRepository.findByOrderNumberWithItems(order.getOrderNumber())
                    .map(CustomerOrder::getOrderItems)
                    .map(orderItems -> orderItems.stream()
                            .map(item -> item.getMenuItem().getName() + " x" + item.getQuantity())
                            .collect(Collectors.joining("; ")))
                    .orElse("No items");

            order.setItems(items); // Ensures OrderDTO holds the correct items

            csvBuilder.append(String.format(
                    "%s,%.2f,%s,%s,%s\n",
                    order.getOrderNumber(),
                    order.getTotalPrice(),
                    order.getStatus(),
                    escapeCsv(order.getCustomer()),
                    escapeCsv(order.getItems())
            ));
        }

        return csvBuilder.toString();
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\n") || input.contains("\"")) {
            input = "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    /**
     * Processes an uploaded CSV file and updates the menu items.
     */
    public static List<String> processMenuCsvFile(MultipartFile file, Long restaurantId, MenuItemService menuItemService) {
        List<String> errorMessages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : parser) {
                try {
                    String name = record.get("name");
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("Missing or empty 'name' field.");
                    }

                    String description = record.isSet("description") ? record.get("description") : "No description provided";
                    double price = record.isSet("price") ? Double.parseDouble(record.get("price")) : 0.0;
                    int inventory = record.isSet("inventory") ? Integer.parseInt(record.get("inventory")) : 0;
                    String ingredients = record.isSet("ingredients") ? record.get("ingredients") : null;
                    String itemId = record.isSet("id") ? record.get("id") : null;

                    MenuItem menuItem = (itemId != null) ?
                            menuItemService.getMenuItemById(Long.parseLong(itemId)).orElse(new MenuItem()) :
                            new MenuItem();

                    menuItem.setName(name);
                    menuItem.setDescription(description);
                    menuItem.setPrice(price);
                    menuItem.setInventory(inventory);
                    menuItem.setIngredients(ingredients);
                    menuItem.setRestaurantId(restaurantId);

                    menuItemService.addMenuItem(menuItem);
                } catch (Exception e) {
                    errorMessages.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errorMessages.add("Failed to process the file: " + e.getMessage());
        }
        return errorMessages;
    }
}
