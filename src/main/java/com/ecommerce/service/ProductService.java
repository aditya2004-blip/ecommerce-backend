package com.ecommerce.service;

import org.springframework.data.domain.Page;

import com.ecommerce.entity.Product;

/**
 * Service interface defining the contract for product catalog management.
 * Provides the business logic for managing product inventory, including 
 * administrative CRUD operations and public-facing search and filtering capabilities.
 */
public interface ProductService {

    /**
     * Persists a new product into the catalog.
     * * @param product The product entity to be saved.
     * @return The persisted Product entity including its generated identifier.
     */
	Product addProduct(Product product);

    /**
     * Updates the details of an existing product.
     * Implementation should handle the synchronization of fields and maintain 
     * data integrity using Optimistic Locking.
     * * @param id The unique identifier of the product to update.
     * @param productDetails The object containing updated product information.
     * @return The updated Product entity.
     * @throws com.ecommerce.exception.ResourceNotFoundException if the product ID is invalid.
     */
    Product updateProduct(Long id, Product productDetails);

    /**
     * Permanently removes a product from the database.
     * * @param id The unique identifier of the product to be deleted.
     * @throws com.ecommerce.exception.ResourceNotFoundException if no product exists with the given ID.
     */
    void deleteProduct(Long id);

    /**
     * Fetches a single product's details by its ID.
     * * @param id The unique identifier of the product.
     * @return The requested Product entity.
     * @throws com.ecommerce.exception.ResourceNotFoundException if the product is not found.
     */
    Product getProductById(Long id);

    /**
     * Retrieves a paginated and optionally filtered list of products.
     * This method is optimized for high-performance browsing by limiting 
     * the result set size per request.
     * * @param page The zero-based page index to retrieve.
     * @param size The number of products to include per page.
     * @param category Optional category filter; if null, all products are returned.
     * @return A Page object containing the list of products and pagination metadata.
     */
    Page<Product> getAllProducts(int page, int size, String category);
}