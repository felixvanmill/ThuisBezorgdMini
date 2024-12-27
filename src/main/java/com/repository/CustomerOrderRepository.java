package com.repository;

import com.model.CustomerOrder;
import com.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    // ✅ Fetch orders by the user's username
    List<CustomerOrder> findByUser_Username(String username);

    // ✅ Fetch orders by status (Updated to OrderStatus Enum)
    List<CustomerOrder> findByStatus(OrderStatus status);

    // ✅ Fetch orders by restaurant ID
    List<CustomerOrder> findByRestaurant_Id(Long restaurantId);

    // ✅ Fetch orders by restaurant ID and status (Updated to OrderStatus Enum)
    List<CustomerOrder> findByRestaurant_IdAndStatus(Long restaurantId, OrderStatus status);

    // ✅ Fetch order with items by ID
    @Query("SELECT o FROM CustomerOrder o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<CustomerOrder> findByIdWithItems(@Param("id") Long id);

    // ✅ Fetch order by ID and restaurant ID
    Optional<CustomerOrder> findByIdAndRestaurant_Id(Long id, Long restaurantId);

    // ✅ Fetch order with items by order number
    @Query("SELECT o FROM CustomerOrder o LEFT JOIN FETCH o.orderItems WHERE o.orderNumber = :orderNumber")
    Optional<CustomerOrder> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    // ✅ For debugging (Updated to OrderStatus Enum)
    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.status = :status")
    long countOrdersByStatus(@Param("status") OrderStatus status);

    // ✅ Fetch orders with full details by multiple statuses (Updated to OrderStatus Enum)
    @Query("SELECT o FROM CustomerOrder o " +
            "LEFT JOIN FETCH o.orderItems i " +
            "LEFT JOIN FETCH i.menuItem " +
            "LEFT JOIN FETCH o.restaurant r " +
            "LEFT JOIN FETCH o.address a " +
            "WHERE o.status IN :statuses")
    List<CustomerOrder> findByStatusesWithDetails(@Param("statuses") List<OrderStatus> statuses);
}
