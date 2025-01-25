package com.repository;

import com.model.AppUser;
import com.repository.AppUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing AppUser entities.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    /**
     * Find a user by username.
     *
     * @param username the username to search for.
     * @return an Optional containing the user if found, or empty if not.
     */
    Optional<AppUser> findByUsername(String username);

}
