package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Layer for the CartItem entity.
 * This interface leverages Spring Data JPA to provide standard CRUD operations 
 * and persistent storage logic for items within a user's shopping cart.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Standard JPA methods like save(), findById(), and delete() are inherited.
}