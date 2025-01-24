# Food Ordering Backend System

This repository contains the backend implementation of a food ordering system designed for small restaurants. The backend provides functionalities for managing orders, menus, inventory, and user roles, while ensuring security and scalability.

## Features

- **Authentication and Authorization:**  
  Secure access using JWT-based authentication and role-based authorization.  
  User roles include:  
  - **Customer**: Places orders and tracks their status.  
  - **Restaurant Employee**: Manages menu items, inventory, and orders.  
  - **Delivery Person**: Views assigned orders and updates their delivery status.

- **Order Management:**  
  Customers can create, manage, and track orders. Orders can have multiple statuses, such as "In Kitchen" and "On the Way."

- **Menu and Inventory Management:**  
  Restaurant employees can:  
  - Add, edit, and delete menu items.  
  - Perform bulk updates for menu items and inventory using CSV files.

- **Delivery Management:**  
  Delivery personnel can view assigned orders, update statuses, and handle cancellations.

- **Data Management:**  
  - User profiles are linked to addresses.  
  - Orders contain detailed information about their items and statuses.

---

## Installation

To run this project locally, follow these steps:

### Prerequisites

- Java Version: 17
- Maven 3.8.1
- PostgreSQL
- IDE: IntelliJ IDEA or another IDE supporting Maven and Java 17.

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/food-ordering-backend.git
   cd food-ordering-backend
2. Set up the database:
    - Create a PostgreSQL database (e.g., food_ordering_db).
    - Update the database configuration in application.properties
   ```bash
   spring.datasource.url=jdbc:postgresql://localhost:5432/food_ordering_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
3. Build and run the application
  ```bash
    mvn clean install
    mvn spring-boot:run
  ```
4. ALL Endpoints can be accessed, downloaded , tested from the datadump out of Postman.

## In case no Postman: API Endpoints

### Authentication

- **POST /login**  
  Authenticate users and return a JWT token.

### Orders

- **POST /orders**  
  Place a new order.
- **GET /orders/{id}**  
  View a specific order's details.
- **PUT /orders/{id}/status**  
  Update the status of an order.

### Menu Management

- **POST /menu-items**  
  Add a new menu item.
- **PUT /menu-items/{id}**  
  Update a menu item's details.
- **DELETE /menu-items/{id}**  
  Remove a menu item.
- **POST /menu-items/bulk**  
  Bulk upload menu items via CSV.

### Delivery

- **GET /deliveries**  
  View assigned orders for a delivery person.
- **PUT /deliveries/{id}/status**  
  Update the delivery status of an order.

## Architecture

The system is built using the following layers:

- **Controller**: Handles API requests and responses.
- **Service**: Contains business logic for processing requests.
- **Repository**: Interacts with the database for CRUD operations.

## Technologies Used

- **Spring Boot**: Backend framework.
- **Spring Security**: For authentication and authorization.
- **JWT**: Token-based security.
- **PostgreSQL**: Database.
- **Maven**: Build automation.

# Future Enhancements

- Implement direct functionality to update menu without CSV. 
- Add Admin functionalities to override certain actions. 



