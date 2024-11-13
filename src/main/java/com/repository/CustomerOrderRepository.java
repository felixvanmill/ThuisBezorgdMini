package com.repository;

import com.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    // Method to find orders by user username
    List<CustomerOrder> findByUser_Username(String username);

    // Method to find orders by status, for example, for querying ASSIGNED or DELIVERED orders
    List<CustomerOrder> findByStatus(String status);

    // New method for querying by restaurantId
    List<CustomerOrder> findByRestaurant_Id(Long restaurantId);
}
