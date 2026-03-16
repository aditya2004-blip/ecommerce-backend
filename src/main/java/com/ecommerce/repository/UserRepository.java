package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Layer for the User entity.
 * This repository manages user account persistence and provides critical
 * query methods for the authentication and registration workflows.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Retrieves a user from the database based on their unique email address.
     * Primarily used during the JWT authentication process to verify identity.
     * * @param email The unique email address of the user.
     * @return An Optional containing the User if found, or empty otherwise.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if a user already exists in the system with the given email.
     * Used during the registration process to prevent duplicate account creation.
     * * @param email The email address to check for existence.
     * @return True if a user with the email exists, false otherwise.
     */
    boolean existsByEmail(String email);
}