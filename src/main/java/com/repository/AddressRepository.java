package com.repository;

import com.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Address entities.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    // JpaRepository provides basic CRUD operations by default.
}
