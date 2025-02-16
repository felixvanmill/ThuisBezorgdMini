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
        assertFalse(users.isEmpty(), "⚠️ De gebruikers tabel is leeg!");

        for (Map<String, Object> user : users) {
            assertNotNull(user.get("username"), "❌ Gebruikersnaam ontbreekt!");
            assertFalse(((String) user.get("username")).isBlank(), "❌ Gebruikersnaam is leeg!");

            assertNotNull(user.get("password"), "❌ Wachtwoord ontbreekt!");
            assertTrue(((String) user.get("password")).length() >= 8, "❌ Wachtwoord is te kort!");

            assertNotNull(user.get("role"), "❌ Rol ontbreekt!");
            assertTrue(List.of("CUSTOMER", "RESTAURANT_EMPLOYEE", "DELIVERY_PERSON").contains(user.get("role")), "❌ Ongeldige rol!");
        }
    }

    @Test
    void testMenuItemsData() {
        List<Map<String, Object>> menuItems = jdbcTemplate.queryForList("SELECT * FROM menu_item");
        assertFalse(menuItems.isEmpty(), "⚠️ Menu items tabel is leeg!");

        for (Map<String, Object> item : menuItems) {
            assertNotNull(item.get("name"), "❌ Naam van menu-item ontbreekt!");
            assertNotNull(item.get("price"), "❌ Prijs ontbreekt!");

            // Fix: Gebruik BigDecimal en converteer naar double
            BigDecimal price = (BigDecimal) item.get("price");
            assertTrue(price.doubleValue() > 0, "❌ Prijs moet groter zijn dan 0!");

            assertNotNull(item.get("inventory"), "❌ Inventaris ontbreekt!");
            assertTrue((Integer) item.get("inventory") >= 0, "❌ Inventaris kan niet negatief zijn!");
        }
    }

    @Test
    void testCustomerOrdersData() {
        List<Map<String, Object>> orders = jdbcTemplate.queryForList("SELECT * FROM customer_order");
        assertFalse(orders.isEmpty(), "⚠️ Bestellingen tabel is leeg!");

        for (Map<String, Object> order : orders) {
            assertNotNull(order.get("order_number"), "❌ Ordernummer ontbreekt!");

            // Fix: Gebruik BigDecimal voor total_price en converteer naar double
            if (order.get("total_price") != null) {
                BigDecimal totalPrice = (BigDecimal) order.get("total_price");
                assertTrue(totalPrice.doubleValue() >= 0, "❌ Ongeldige prijs!");
            }

            assertNotNull(order.get("status"), "❌ Status ontbreekt!");
            assertTrue(List.of("UNCONFIRMED", "CONFIRMED", "PICKING_UP", "TRANSPORT", "DELIVERED", "READY_FOR_DELIVERY", "IN_KITCHEN", "CANCELED").contains(order.get("status")), "❌ Ongeldige status!");
        }
    }

    @Test
    void testAddressTableData() {
        List<Map<String, Object>> addresses = jdbcTemplate.queryForList("SELECT * FROM address");
        assertFalse(addresses.isEmpty(), "⚠️ De adres tabel is leeg!");

        for (Map<String, Object> address : addresses) {
            assertNotNull(address.get("street_name"), "❌ Straatnaam ontbreekt!");
            assertNotNull(address.get("house_number"), "❌ Huisnummer ontbreekt!");
            assertNotNull(address.get("postal_code"), "❌ Postcode ontbreekt!");
            assertNotNull(address.get("city"), "❌ Stad ontbreekt!");
        }
    }
}
