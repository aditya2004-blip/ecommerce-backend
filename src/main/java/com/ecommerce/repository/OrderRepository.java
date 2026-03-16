package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Layer for the Order entity.
 * This repository manages the persistence of customer purchase history 
 * and provides specialized query methods for retrieving order summaries.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Retrieves a complete history of orders associated with a specific user.
     * Used primarily for the 'My Orders' section of the user profile.
     * * @param userId The unique identifier of the user whose orders are being retrieved.
     * @return A list of Order entities placed by the specified user.
     */
    List<Order> findByUserId(Long userId);
}