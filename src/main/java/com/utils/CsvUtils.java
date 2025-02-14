package com.utils;

import com.dto.OrderDTO;
import com.model.CustomerOrder;
import com.repository.CustomerOrderRepository;
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
}
