package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Layer for the Cart entity.
 * This repository manages the lifecycle of the shopping cart and provides
 * specialized query methods to retrieve cart data based on user ownership.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Retrieves the shopping cart associated with a specific user.
     * Used primarily for cart synchronization and checkout processing.
     * * @param userId The unique identifier of the owner.
     * @return An Optional containing the Cart if found, or empty otherwise.
     */
    Optional<Cart> findByUserId(Long userId);
}