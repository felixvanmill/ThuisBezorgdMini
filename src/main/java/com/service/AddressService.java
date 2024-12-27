// src/main/java/com/service/AddressService.java

package com.service;

import com.model.Address;
import com.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    /**
     * Retrieve an address by its ID.
     * @param id Address ID.
     * @return Address if found.
     */
    public Address getAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found for ID: " + id));
    }

    /**
     * Create a new address.
     * @param address Address object to save.
     * @return Saved address.
     */
    public Address createAddress(Address address) {
        return addressRepository.save(address);
    }
}
