-- Drop tables if they are currently existing
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS customer_order CASCADE;
DROP TABLE IF EXISTS menu_item CASCADE;
DROP TABLE IF EXISTS app_users CASCADE;
DROP TABLE IF EXISTS restaurant CASCADE;
DROP TABLE IF EXISTS address CASCADE;

-- Enable the pgcrypto extension
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Table: address
CREATE TABLE IF NOT EXISTS address (
                                       id SERIAL PRIMARY KEY,
                                       street_name VARCHAR(255) NOT NULL,
                                       house_number VARCHAR(10) NOT NULL,
                                       postal_code VARCHAR(20) NOT NULL,
                                       city VARCHAR(255) NOT NULL
);

-- Table: restaurant
CREATE TABLE IF NOT EXISTS restaurant (
                                          id SERIAL PRIMARY KEY,
                                          name VARCHAR(255) NOT NULL,
                                          description VARCHAR(255),
                                          location VARCHAR(255),
                                          slug VARCHAR(255) NOT NULL UNIQUE
);

-- Table: app_users
CREATE TABLE IF NOT EXISTS app_users (
                                         id SERIAL PRIMARY KEY,
                                         username VARCHAR(255) NOT NULL UNIQUE,
                                         password VARCHAR(255) NOT NULL,
                                         role VARCHAR(255) NOT NULL CHECK (role IN ('CUSTOMER', 'RESTAURANT_EMPLOYEE', 'DELIVERY_PERSON')),
                                         full_name VARCHAR(255),
                                         address_id INT REFERENCES address(id) ON DELETE SET NULL ON UPDATE CASCADE,
                                         restaurant_id INT REFERENCES restaurant(id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- Table: menu_item
CREATE TABLE IF NOT EXISTS menu_item (
                                         id SERIAL PRIMARY KEY,
                                         name VARCHAR(255) NOT NULL,
                                         description VARCHAR(255),
                                         price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
                                         ingredients VARCHAR(255),
                                         inventory INT NOT NULL CHECK (inventory >= 0),
                                         is_available BOOLEAN DEFAULT TRUE, -- Add this column for availability
                                         restaurant_id INT REFERENCES restaurant(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Table: customer_order
CREATE TABLE IF NOT EXISTS customer_order (
                                              id SERIAL PRIMARY KEY,
                                              user_id INT REFERENCES app_users(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                              address_id INT REFERENCES address(id) ON DELETE SET NULL ON UPDATE CASCADE,
                                              status VARCHAR(255) NOT NULL CHECK (status IN ('UNCONFIRMED', 'CONFIRMED', 'PICKING_UP', 'TRANSPORT', 'DELIVERED', 'READY_FOR_DELIVERY', 'IN_KITCHEN', 'CANCELED')), -- Ensure 'CANCELED' is included
                                              total_price DECIMAL(10, 2) CHECK (total_price >= 0),
                                              restaurant_id INT REFERENCES restaurant(id) ON DELETE SET NULL ON UPDATE CASCADE,
                                              order_number VARCHAR(255) NOT NULL UNIQUE,
                                              delivery_person VARCHAR(255) DEFAULT NULL
);

-- Table: order_items
CREATE TABLE IF NOT EXISTS order_items (
                                           id SERIAL PRIMARY KEY,
                                           customer_order_id INT REFERENCES customer_order(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                           menu_item_id INT REFERENCES menu_item(id) ON DELETE CASCADE ON UPDATE CASCADE,
                                           quantity INT NOT NULL CHECK (quantity > 0),
                                           order_number VARCHAR(255)
);
