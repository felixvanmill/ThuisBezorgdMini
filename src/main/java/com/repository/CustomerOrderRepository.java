package com.repository;

import com.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    // Method to find orders by user username
    List<CustomerOrder> findByUser_Username(String username);

    // Method to find orders by status, for example, for querying ASSIGNED or DELIVERED orders
    List<CustomerOrder> findByStatus(String status);

    // New method for querying by restaurantId
    List<CustomerOrder> findByRestaurant_Id(Long restaurantId);

    List<CustomerOrder> findByRestaurant_IdAndStatus(Long restaurantId, String status); // Optional

    @Query("SELECT o FROM CustomerOrder o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<CustomerOrder> findByIdWithItems(@Param("id") Long id);
}
