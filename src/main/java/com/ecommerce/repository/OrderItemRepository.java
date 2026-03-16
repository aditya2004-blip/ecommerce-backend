package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Layer for the OrderItem entity.
 * This interface provides the persistence logic for individual line items 
 * within a finalized order, linking specific products to their purchase price and quantity.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Inherits standard CRUD functionality from JpaRepository for managing order line items.
}