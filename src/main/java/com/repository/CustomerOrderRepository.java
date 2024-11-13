package com.repository;

import com.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    // Existing methods
    List<CustomerOrder> findByDeliveryPersonUsername(String username);
    List<CustomerOrder> findByUser_Username(String username);

    // New method for querying by restaurantId
    List<CustomerOrder> findByRestaurant_Id(Long restaurantId);
}
