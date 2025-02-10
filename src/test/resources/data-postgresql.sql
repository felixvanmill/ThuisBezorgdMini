-- Insert data into address
INSERT INTO address (street_name, house_number, postal_code, city)
VALUES
    ('Customer Lane', '123', '12345', 'City'),
    ('Another St', '456', '67890', 'City')
    ON CONFLICT DO NOTHING;

-- Create and populate the restaurant table with a slug column
INSERT INTO restaurant (id, name, description, location, slug)
VALUES
    (1, 'Pizza Place', 'Pizzeria specializing in Italian dishes', '123 Main St, City', 'pizza-place'),
    (2, 'Sushi World', 'Authentic Japanese restaurant with fresh sushi', '456 Ocean Ave, City', 'sushi-world')
    ON CONFLICT DO NOTHING;

-- Update other tables as needed

-- Insert data into app_users
INSERT INTO app_users (username, password, role, full_name, address_id, restaurant_id)
VALUES
    ('johndoe', '$2a$10$VV7hM4ueF.XpVP0VOJtJL.L1N2nb9jwPtxp0KNPP.9MPJahd49.2S', 'CUSTOMER', 'John Doe', 1, NULL),
    ('marysmith', '$2a$10$VV7hM4ueF.XpVP0VOJtJL.L1N2nb9jwPtxp0KNPP.9MPJahd49.2S', 'RESTAURANT_EMPLOYEE', 'Mary Smith', NULL, 1),
    ('pizzachef', '$2a$10$VV7hM4ueF.XpVP0VOJtJL.L1N2nb9jwPtxp0KNPP.9MPJahd49.2S', 'RESTAURANT_EMPLOYEE', 'Pizza Chef', NULL, 2),
    ('alexjohnson', '$2a$10$VV7hM4ueF.XpVP0VOJtJL.L1N2nb9jwPtxp0KNPP.9MPJahd49.2S', 'DELIVERY_PERSON', 'Alex Johnson', NULL, NULL)
    ON CONFLICT DO NOTHING;

-- Insert data into menu_item
INSERT INTO menu_item (name, description, price, ingredients, inventory, is_available, restaurant_id)
VALUES
    ('Margherita Pizza', 'Classic cheese and tomato', 9.99, 'Cheese, Tomato, Basil', 999, TRUE, 1),
    ('Pepperoni Pizza', 'Cheese, tomato, and pepperoni', 11.99, 'Cheese, Tomato, Pepperoni', 999, TRUE, 1),
    ('California Roll', 'Crab, avocado, and cucumber', 8.99, 'Crab, Avocado, Cucumber', 999, TRUE, 2),
    ('Spicy Tuna Roll', 'Tuna with spicy sauce', 10.99, 'Tuna, Spicy Mayo', 999, TRUE, 2)
    ON CONFLICT DO NOTHING;

-- Insert data into customer_order
INSERT INTO customer_order (user_id, address_id, status, total_price, restaurant_id, order_number, delivery_person)
VALUES
    (1, 1, 'READY_FOR_DELIVERY', 31.97, 1, 'ORDER001', NULL),
    (1, 2, 'DELIVERED', 47.95, 2, 'ORDER002', 'alexjohnson'),
    -- New test order for delivery person functionality
    (1, 1, 'READY_FOR_DELIVERY', 25.98, 1, 'ORDER003', NULL)
    ON CONFLICT DO NOTHING;

-- Insert data into order_items
INSERT INTO order_items (customer_order_id, menu_item_id, quantity, order_number)
VALUES
    (1, 1, 2, 'ORDER001'),
    (1, 2, 1, 'ORDER001'),
    (2, 3, 3, 'ORDER002'),
    (2, 4, 2, 'ORDER002'),
    -- Items for the new test order
    (3, 1, 1, 'ORDER003'),
    (3, 2, 1, 'ORDER003')
    ON CONFLICT DO NOTHING;
