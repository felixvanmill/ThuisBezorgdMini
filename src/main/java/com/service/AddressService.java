// src/main/java/com/service/AddressService.java

package com.service;

import com.model.Address;
import com.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing addresses.
 */
@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    /**
     * Find an address by its ID.
     *
     * @param id The ID of the address to retrieve.
     * @return The found address.
     * @throws RuntimeException if the address is not found.
     */
    public Address getAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found for ID: " + id));
    }

    /**
     * Save a new address to the database.
     *
     * @param address The address to save.
     * @return The saved address.
     */
    public Address createAddress(Address address) {
        return addressRepository.save(address);
    }
}
