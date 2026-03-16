package com.ecommerce.serviceImplTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.ecommerce.serviceImpl.RedisInventoryServiceImpl;

/**
 * Unit test suite for {@link RedisInventoryServiceImpl}.
 * This class validates the high-performance inventory management logic, ensuring 
 * that Redis atomic operations (decrement/increment) are correctly utilized to 
 * prevent overselling in high-concurrency environments.
 */
@ExtendWith(MockitoExtension.class)
public class RedisInventoryServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    /**
     * The service under test with the mocked Redis template operations injected.
     */
    @InjectMocks
    private RedisInventoryServiceImpl redisInventoryService;

    /**
     * Configures the mock Redis template to return specialized value operations 
     * before each test case, enabling the simulation of key-value interactions.
     */
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * Verifies that the stock levels from the primary database are correctly 
     * serialized and synchronized with the Redis cache.
     */
    @Test
    @DisplayName("Set Stock - Should correctly save string value to Redis")
    void testSetStock() {
        // Act
        redisInventoryService.setStock(5L, 100);

        // Assert: Ensure the correct key pattern and string value were used
        verify(valueOperations, times(1)).set("product:stock:5", "100");
    }

    /**
     * Validates the successful reservation flow.
     * Checks that when a decrement results in a non-negative number, 
     * the service confirms the reservation.
     */
    @Test
    @DisplayName("Reserve Stock - Should return true when stock is sufficient")
    void testReserveStock_Success() {
        // Arrange: Simulate enough stock remaining (5 units left)
        when(valueOperations.decrement("product:stock:5", 2)).thenReturn(5L);

        // Act
        boolean result = redisInventoryService.reserveStock(5L, 2);

        // Assert
        assertTrue(result, "Reservation should be successful for positive remaining stock");
        verify(valueOperations, times(1)).decrement("product:stock:5", 2);
    }

    /**
     * Tests the "Oversell Protection" logic.
     * Verifies that if a decrement results in a negative value (indicating 
     * stock depletion), the service:
     * 1. Returns false to the caller.
     * 2. Issues an atomic increment to "rollback" the stock count in Redis.
     */
    @Test
    @DisplayName("Reserve Stock - Should return false and increment back when oversold")
    void testReserveStock_Oversold() {
        // Arrange: Simulate stock going below zero (-1) after decrement
        when(valueOperations.decrement("product:stock:5", 2)).thenReturn(-1L);

        // Act
        boolean result = redisInventoryService.reserveStock(5L, 2);

        // Assert
        assertFalse(result, "Reservation should fail if stock would go negative");
        verify(valueOperations, times(1)).decrement("product:stock:5", 2);
        
        // Critical: Verify the self-healing rollback increment
        verify(valueOperations, times(1)).increment("product:stock:5", 2);
    }
}