package com.repository;

import com.dto.OrderDTO;
import com.model.CustomerOrder;
import com.model.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing CustomerOrder entities.
 */
@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    /**
     * Fetch all orders with associated order items, restaurant, and address.
     */
    @EntityGraph(attributePaths = {"orderItems", "restaurant", "address"})
    List<CustomerOrder> findAll();

    /**
     * Fetch a specific order by ID with associated order items.
     */
    @EntityGraph(attributePaths = "orderItems")
    Optional<CustomerOrder> findById(Long id);

    /**
     * Fetch a specific order by ID and restaurant ID with associated order items.
     */
    @EntityGraph(attributePaths = "orderItems")
    Optional<CustomerOrder> findByIdAndRestaurant_Id(Long id, Long restaurantId);

    /**
     * Fetch all orders for a specific restaurant by its ID.
     */
    List<CustomerOrder> findByRestaurant_Id(Long restaurantId);

    /**
     * Fetch orders with specified statuses, including detailed associations.
     */
    @Query("""
        SELECT o FROM CustomerOrder o
        LEFT JOIN FETCH o.orderItems i
        LEFT JOIN FETCH i.menuItem
        LEFT JOIN FETCH o.restaurant r
        LEFT JOIN FETCH o.address a
        WHERE o.status IN :statuses
    """)
    List<CustomerOrder> findByStatusesWithDetails(@Param("statuses") List<OrderStatus> statuses);

    /**
     * Fetch a specific order by its order number, including order items and restaurant.
     */
    @EntityGraph(attributePaths = {"orderItems", "restaurant"})
    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    /**
     * Fetch all orders with a specific status.
     */
    List<CustomerOrder> findByStatus(OrderStatus status);

    /**
     * Fetch orders for a specific user, including associated order items, restaurant, and address.
     */
    @EntityGraph(attributePaths = {"orderItems", "restaurant", "address"})
    List<CustomerOrder> findByUser_Username(String username);

    /**
     * Fetch a specific order by its order number and username, with detailed associations.
     */
    @EntityGraph(attributePaths = {"user", "orderItems", "restaurant", "address"})
    Optional<CustomerOrder> findByOrderNumberAndUser_Username(String orderNumber, String username);


    /**
     * Fetch orders assigned to a delivery person with specified statuses.
     */
    @Query("""
        SELECT o FROM CustomerOrder o
        WHERE o.deliveryPerson = :username AND o.status IN :statuses
    """)
    List<CustomerOrder> findByDeliveryPersonAndStatuses(
            @Param("username") String username,
            @Param("statuses") List<OrderStatus> statuses
    );

    /**
     * Fetch unassigned orders by status, including associated data.
     */
    @Query("""
        SELECT o FROM CustomerOrder o
        LEFT JOIN FETCH o.orderItems i
        LEFT JOIN FETCH i.menuItem
        LEFT JOIN FETCH o.restaurant r
        LEFT JOIN FETCH o.address a
        WHERE o.status = :status AND o.deliveryPerson IS NULL
    """)
    List<CustomerOrder> findUnassignedOrdersByStatus(@Param("status") OrderStatus status);

    /**
     * Fetch a specific order by ID with detailed associations.
     */
    @EntityGraph(attributePaths = {"address", "orderItems.menuItem", "restaurant"})
    @Query("SELECT o FROM CustomerOrder o WHERE o.id = :id")
    Optional<CustomerOrder> findByIdWithDetails(@Param("id") Long id);

    /**
     * Fetch a specific order by order number with detailed associations.
     */
    @Query("""
    SELECT o FROM CustomerOrder o
    LEFT JOIN FETCH o.user u
    LEFT JOIN FETCH u.address
    LEFT JOIN FETCH o.restaurant r
    LEFT JOIN FETCH o.address a
    LEFT JOIN FETCH o.orderItems oi
    LEFT JOIN FETCH oi.menuItem
    WHERE o.orderNumber = :orderNumber
""")
    Optional<CustomerOrder> findByOrderNumberWithDetails(@Param("orderNumber") String orderNumber);




    /**
     * Fetch a specific order by ID and username, with detailed associations.
     */
    @EntityGraph(attributePaths = {"orderItems", "restaurant", "address"})
    Optional<CustomerOrder> findByIdAndUser_Username(Long id, String username);

    /**
     * Fetch orders for a specific restaurant, projecting results into OrderDTO objects.
     */
    @Query("""
        SELECT new com.dto.OrderDTO(
            CAST(o.orderNumber AS string),
            o.totalPrice,
            o.status,
            u.fullName,
            ''
        )
        FROM CustomerOrder o
        JOIN o.user u
        WHERE o.restaurant.id = :restaurantId
    """)
    List<OrderDTO> findByRestaurant_IdWithDetails(@Param("restaurantId") Long restaurantId);

    /**
     * Fetch a specific order by order number, including associated order items and menu items.
     */
    @Query("""
        SELECT o FROM CustomerOrder o
        LEFT JOIN FETCH o.orderItems i
        LEFT JOIN FETCH i.menuItem
        WHERE o.orderNumber = :orderNumber
    """)
    Optional<CustomerOrder> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    /**
     * Explicit method to fetch an order by its ID.
     *
     * @param id The ID of the order.
     * @return The order if found.
     */
    @Query("SELECT o FROM CustomerOrder o WHERE o.id = :id")
    Optional<CustomerOrder> findCustomerOrderById(@Param("id") Long id);


}
