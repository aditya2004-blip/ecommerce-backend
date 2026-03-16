package com.ecommerce.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing high-performance inventory tracking using Redis.
 * This service provides atomic operations to handle stock reservations, preventing 
 * overselling in high-concurrency environments by utilizing Redis's atomic decrement.
 */
@Service
public class RedisInventoryServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(RedisInventoryServiceImpl.class);
    private final StringRedisTemplate redisTemplate;

    /**
     * Constructs the RedisInventoryService with a specialized template for String-based operations.
     */
    @Autowired
    public RedisInventoryServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Synchronizes the Redis cache with the current stock levels from the primary database.
     * Typically called during product updates or initial application warm-up.
     * * @param productId The unique identifier of the product.
     * @param stock The current available quantity.
     */
    public void setStock(Long productId, Integer stock) {
        String redisKey = "product:stock:" + productId;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(stock));
        logger.info("Synchronized Redis: Product ID {} now has {} stock.", productId, stock);
    }
    
    /**
     * Attempts to atomically reserve a specific quantity of stock for a product.
     * This method uses the Redis DECRBY operation to ensure thread-safety without 
     * explicit database locking.
     * * @param productId The unique identifier of the product.
     * @param quantity The number of units to reserve.
     * @return true if stock was successfully reserved; false if insufficient stock exists.
     */
    public boolean reserveStock(Long productId, int quantity) {
        String redisKey = "product:stock:" + productId;
        
        // Atomically decrement the stock count
        Long remainingStock = redisTemplate.opsForValue().decrement(redisKey, quantity);
        
        if (remainingStock != null && remainingStock >= 0) {
            logger.debug("Successfully reserved {} units of Product ID: {} in Redis. Remaining: {}", 
                    quantity, productId, remainingStock);
            return true;
        } else {
            // Rollback the decrement if the result was negative (insufficient stock)
            redisTemplate.opsForValue().increment(redisKey, quantity);
            logger.warn("Failed to reserve stock in Redis for Product ID: {}. Out of stock.", productId);
            return false;
        }
    }
}