package com.ecommerce.config;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.serviceImpl.RedisInventoryServiceImpl;

/**
 * Unit test for the Redis Cache Warming process.
 * This class verifies that on application startup, the system correctly retrieves 
 * all product stock levels from the relational database and synchronizes them 
 * with the Redis high-performance cache.
 */
@ExtendWith(MockitoExtension.class)
public class RedisCacheWarmingConfigTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RedisInventoryServiceImpl redisInventoryService;

    /**
     * The instance of the cache warming logic being tested, with mocked 
     * dependencies automatically injected.
     */
    @InjectMocks
    private RedisCacheWarmingConfig redisCacheWarming;

    /**
     * Ensures that the cache warming logic iterates through all products and 
     * triggers the Redis 'setStock' operation for each item found in the database.
     * * This test guarantees that the cache is never empty upon application launch, 
     * preventing initial requests from hitting the database for stock checks.
     */
    @Test
    @DisplayName("Cache Warming - Should load all products into Redis on startup")
    void testLoadStockIntoRedisOnStartup() {
        // Arrange: Setup mock data for products
        Product p1 = new Product();
        p1.setId(1L);
        p1.setStock(50);

        Product p2 = new Product();
        p2.setId(2L);
        p2.setStock(10);

        // Define mock behavior: return the product list when repository is queried
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // Act: Trigger the cache warming method
        redisCacheWarming.loadStockIntoRedisOnStartup();

        // Assert: Verify that the database was queried exactly once
        verify(productRepository, times(1)).findAll();
        
        // Assert: Verify each product was individualy synchronized with the Redis service
        verify(redisInventoryService, times(1)).setStock(1L, 50);
        verify(redisInventoryService, times(1)).setStock(2L, 10);
    }
}