// src/test/java/com/service/AddressServiceTest.java

package com.service;

import com.model.Address;
import com.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    public AddressServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test retrieving an address by its ID.
     * Ensures the service returns the correct address.
     */
    @Test
    void testGetAddressById() {
        final Address address = new Address("Main St", "123", "12345", "City");
        when(this.addressRepository.findById(1L)).thenReturn(Optional.of(address));

        final Address result = this.addressService.getAddressById(1L);

        assertNotNull(result);
        assertEquals("Main St", result.getStreetName());
    }

    /**
     * Test retrieving a non-existing address.
     * Ensures an exception is thrown when the address is not found.
     */
    @Test
    void testGetAddressById_NotFound() {
        when(this.addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> this.addressService.getAddressById(1L));
    }

    /**
     * Test creating a new address.
     * Ensures the service can save and return a new address.
     */
    @Test
    void testCreateAddress() {
        final Address address = new Address("Main St", "123", "12345", "City");
        when(this.addressRepository.save(any(Address.class))).thenReturn(address);

        final Address result = this.addressService.createAddress(address);

        assertNotNull(result);
        assertEquals("Main St", result.getStreetName());
    }
}
