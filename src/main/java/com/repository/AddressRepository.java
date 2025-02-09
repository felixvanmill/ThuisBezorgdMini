package com.repository;

import com.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing Address entities.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    /**
     * Alias method for findById to make it explicitly visible in the repository.
     *
     * @param id The ID of the address.
     * @return The address, if found.
     */
    Optional<Address> findAddressById(Long id);
}