package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data Access Layer for the Product entity.
 * This repository handles the core persistence logic for the product catalog,
 * including support for pagination, sorting, and category-based filtering.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Retrieves a paginated list of products filtered by their category name.
     * This method utilizes Spring Data JPA's query derivation to automatically
     * generate the necessary SQL for category filtering and pagination.
     * * @param category The name of the category to filter products by.
     * @param pageable The pagination information (page number, size, and sorting).
     * @return A Page of Product entities matching the specified category.
     */
    Page<Product> findByCategory(String category, Pageable pageable);
}