package com.ecommerce.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;

/**
 * Implementation of the {@link ProductService} for managing the product catalog.
 * This service coordinates database persistence via {@link ProductRepository} and 
 * ensures real-time inventory synchronization with {@link RedisInventoryServiceImpl} 
 * to handle high-concurrency stock checks.
 */
@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

	private final ProductRepository productRepository;
	private final RedisInventoryServiceImpl redisInventoryService;

	/**
	 * Constructor-based injection for the database repository and Redis cache service.
	 */
	@Autowired
	public ProductServiceImpl(ProductRepository productRepository, RedisInventoryServiceImpl redisInventoryService) {
		this.productRepository = productRepository;
		this.redisInventoryService = redisInventoryService;
	}

	/**
	 * Adds a new product to the database and initializes its stock levels in Redis.
	 * * @param product The product details to persist.
	 * @return The saved Product entity.
	 */
	@Override
	public Product addProduct(Product product) {
		logger.info("Attempting to add a new product to the catalog: {}", product.getName());
		Product savedProduct = productRepository.save(product);
		
		// Synchronize the new product's stock with Redis cache
		redisInventoryService.setStock(savedProduct.getId(), savedProduct.getStock());
		
		logger.info("Successfully added new product. Assigned ID: {}", savedProduct.getId());
		return savedProduct;
	}

	/**
	 * Updates an existing product's metadata and refreshes its stock level in the Redis cache.
	 * * @param id The ID of the product to modify.
	 * @param productDetails The updated product values.
	 * @return The updated Product entity.
	 * @throws ResourceNotFoundException if the product ID does not exist.
	 */
	@Override
	public Product updateProduct(Long id, Product productDetails) {
		logger.info("Attempting to update product with ID: {}", id);
		
		Product existingProduct = getProductById(id);
		logger.debug("Updating fields for product ID: {}", id);
		
		existingProduct.setName(productDetails.getName());
		existingProduct.setDescription(productDetails.getDescription());
		existingProduct.setPrice(productDetails.getPrice());
		existingProduct.setStock(productDetails.getStock());
		existingProduct.setCategory(productDetails.getCategory());
		existingProduct.setImageUrl(productDetails.getImageUrl());

        Product updatedProduct = productRepository.save(existingProduct);
        
        // Ensure the high-performance cache reflects the updated stock level
        redisInventoryService.setStock(updatedProduct.getId(), updatedProduct.getStock());
        
        logger.info("Successfully updated product with ID: {}", id);
        return updatedProduct;
	}

	/**
	 * Removes a product from the database catalog.
	 * * @param id The ID of the product to be deleted.
	 */
	@Override
	public void deleteProduct(Long id) {
		logger.info("Attempting to delete product with ID: {}", id);
        
        Product existingProduct = getProductById(id);
        productRepository.delete(existingProduct);    
        
        logger.info("Successfully deleted product with ID: {}", id);
	}

	/**
	 * Retrieves a single product by its unique identifier.
	 * * @param id The ID of the product.
	 * @return The Product entity.
	 * @throws ResourceNotFoundException if the product is not found.
	 */
	@Override
	public Product getProductById(Long id) {
		logger.debug("Fetching product from database with ID: {}", id);
		
		return productRepository.findById(id)
				.orElseThrow(() -> {
					logger.error("Product retrieval failed. No product exists with ID: {}", id);
					return new ResourceNotFoundException("Product not found with id: " + id);
				});
	}

	/**
	 * Retrieves products using pagination and optional category filtering.
	 * * @param page Zero-based page index.
	 * @param size Number of items per page.
	 * @param category Optional category name for filtering.
	 * @return A Page of Product entities.
	 */
	@Override
	public Page<Product> getAllProducts(int page, int size, String category) {
		
		logger.info("Fetching products - Page: {}, Size: {}, Category filter: {}", 
                page, size, (category != null ? category : "NONE"));
		
		Pageable pageable = PageRequest.of(page, size);
		
		if (category != null && !category.isEmpty()) {
			logger.debug("Executing database query with category filter: {}", category);
			return productRepository.findByCategory(category, pageable);
		}
		
		logger.debug("Executing un-filtered database query to fetch all products.");
        return productRepository.findAll(pageable);
	}
}