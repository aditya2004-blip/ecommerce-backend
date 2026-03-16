package com.ecommerce.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.serviceImpl.RedisInventoryServiceImpl;


/**
 * Component responsible for pre-loading database values into the Redis cache.
 * This process, known as "Cache Warming," ensures the application is optimized 
 * for high-concurrency traffic immediately upon startup.
 */


@Component
public class RedisCacheWarmingConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheWarmingConfig.class);
    
    private final ProductRepository productRepository;
    private final RedisInventoryServiceImpl redisInventoryService;

    /**
     * Constructor injection for required dependencies.
     */
    
    @Autowired
    public RedisCacheWarmingConfig(ProductRepository productRepository, RedisInventoryServiceImpl redisInventoryService) {
        this.productRepository = productRepository;
        this.redisInventoryService = redisInventoryService;
    }

    /**
     * Listens for the ApplicationReadyEvent to trigger inventory migration.
     * Fetches current stock levels from MySQL and synchronizes them with Redis
     * to enable atomic inventory operations during high-traffic sales.
     */
    
    @EventListener(ApplicationReadyEvent.class)
    public void loadStockIntoRedisOnStartup() {
        logger.info("Starting Redis Cache Warming...");
        
     // Retrieve all products from the  database
        List<Product> allProducts = productRepository.findAll();
        
     // Synchronize each product's stock level to the Redis in-memory store
        for (Product product : allProducts) {
            redisInventoryService.setStock(product.getId(), product.getStock());
        }
        
        logger.info("Successfully loaded {} products into Redis. System is ready for Sales!", allProducts.size());
    }
}