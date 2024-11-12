package com.repository;

import com.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    // Custom query for finding by delivery person's username (no change needed here)
    List<CustomerOrder> findByDeliveryPersonUsername(String username);

    // Adjusting this method to refer to 'user.username' using the underscore syntax for nested properties
    List<CustomerOrder> findByUser_Username(String username);  // Updated query method
}
