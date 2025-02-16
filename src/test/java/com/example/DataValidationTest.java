package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DataValidationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testUsersTableData() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT * FROM app_users");
        assertFalse(users.isEmpty(), "⚠️ The users table is empty!");

        for (Map<String, Object> user : users) {
            assertNotNull(user.get("username"), "❌ Username is missing!");
            assertFalse(((String) user.get("username")).isBlank(), "❌ Username is empty!");

            assertNotNull(user.get("password"), "❌ Password is missing!");
            assertTrue(((String) user.get("password")).length() >= 8, "❌ Password is too short!");

            assertNotNull(user.get("role"), "❌ Role is missing!");
            assertTrue(List.of("CUSTOMER", "RESTAURANT_EMPLOYEE", "DELIVERY_PERSON").contains(user.get("role")), "❌ Invalid role!");
        }
    }

    @Test
    void testMenuItemsData() {
        List<Map<String, Object>> menuItems = jdbcTemplate.queryForList("SELECT * FROM menu_item");
        assertFalse(menuItems.isEmpty(), "⚠️ The menu items table is empty!");

        for (Map<String, Object> item : menuItems) {
            assertNotNull(item.get("name"), "❌ Menu item name is missing!");
            assertNotNull(item.get("price"), "❌ Price is missing!");

            // Fix: Use BigDecimal and convert to double
            BigDecimal price = (BigDecimal) item.get("price");
            assertTrue(price.doubleValue() > 0, "❌ Price must be greater than 0!");

            assertNotNull(item.get("inventory"), "❌ Inventory is missing!");
            assertTrue((Integer) item.get("inventory") >= 0, "❌ Inventory cannot be negative!");
        }
    }

    @Test
    void testCustomerOrdersData() {
        List<Map<String, Object>> orders = jdbcTemplate.queryForList("SELECT * FROM customer_order");
        assertFalse(orders.isEmpty(), "⚠️ The orders table is empty!");

        for (Map<String, Object> order : orders) {
            assertNotNull(order.get("order_number"), "❌ Order number is missing!");

            // Fix: Use BigDecimal for total_price and convert to double
            if (order.get("total_price") != null) {
                BigDecimal totalPrice = (BigDecimal) order.get("total_price");
                assertTrue(totalPrice.doubleValue() >= 0, "❌ Invalid price!");
            }

            assertNotNull(order.get("status"), "❌ Status is missing!");
            assertTrue(List.of("UNCONFIRMED", "CONFIRMED", "PICKING_UP", "TRANSPORT", "DELIVERED", "READY_FOR_DELIVERY", "IN_KITCHEN", "CANCELED").contains(order.get("status")), "❌ Invalid status!");
        }
    }

    @Test
    void testAddressTableData() {
        List<Map<String, Object>> addresses = jdbcTemplate.queryForList("SELECT * FROM address");
        assertFalse(addresses.isEmpty(), "⚠️ The address table is empty!");

        for (Map<String, Object> address : addresses) {
            assertNotNull(address.get("street_name"), "❌ Street name is missing!");
            assertNotNull(address.get("house_number"), "❌ House number is missing!");
            assertNotNull(address.get("postal_code"), "❌ Postal code is missing!");
            assertNotNull(address.get("city"), "❌ City is missing!");
        }
    }
}
