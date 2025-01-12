package com.repository;

import com.model.CustomerOrder;
import com.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {"orderItems", "restaurant", "address"})
    List<CustomerOrder> findAll();

    @EntityGraph(attributePaths = {"orderItems"})
    Optional<CustomerOrder> findById(Long id);

    @EntityGraph(attributePaths = {"orderItems"})
    Optional<CustomerOrder> findByIdAndRestaurant_Id(Long id, Long restaurantId);

    List<CustomerOrder> findByRestaurant_Id(Long restaurantId);

    @Query("SELECT o FROM CustomerOrder o " +
            "LEFT JOIN FETCH o.orderItems i " +
            "LEFT JOIN FETCH i.menuItem " +
            "LEFT JOIN FETCH o.restaurant r " +
            "LEFT JOIN FETCH o.address a " +
            "WHERE o.status IN :statuses")
    List<CustomerOrder> findByStatusesWithDetails(@Param("statuses") List<OrderStatus> statuses);

    @EntityGraph(attributePaths = {"orderItems", "restaurant", "address"})
    Optional<CustomerOrder> findByOrderNumber(String orderNumber);
}

