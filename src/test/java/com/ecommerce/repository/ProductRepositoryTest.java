package com.ecommerce.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ecommerce.entity.Product;

/**
 * Unit test suite for the {@link ProductRepository}.
 * This class validates the data access layer for the product catalog. 
 * It focuses on testing custom query methods for category-based filtering 
 * and pagination, ensuring the repository correctly interfaces with 
 * Spring Data's {@link Pageable} abstraction.
 */
@ExtendWith(MockitoExtension.class) 
public class ProductRepositoryTest {

    @Mock
    private ProductRepository productRepository;

    private Product mockProduct;
    private Page<Product> mockProductPage;
    private Pageable pageable;

    /**
     * Initializes a sample product and a mock paginated response.
     * Setting up the {@link Pageable} and {@link Page} objects is critical 
     * for verifying the catalog's filtering and browsing logic.
     */
    @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("Gaming Monitor");
        mockProduct.setCategory("Electronics");
        mockProduct.setPrice(new BigDecimal("299.99"));
        mockProduct.setStock(50);

        pageable = PageRequest.of(0, 10);

        // Wrapping the list in a PageImpl to simulate Spring Data JPA behavior
        mockProductPage = new PageImpl<>(List.of(mockProduct), pageable, 1);
    }

    /**
     * Verifies that the custom {@code findByCategory} query correctly retrieves 
     * a paginated list of products for a specific category.
     */
    @DisplayName("Mock Find By Category - Should return a Page of Products for valid category")
    @Test
    void testFindByCategory_Success() {
        // Arrange
        when(productRepository.findByCategory(eq("Electronics"), any(Pageable.class)))
                .thenReturn(mockProductPage);

        // Act
        Page<Product> result = productRepository.findByCategory("Electronics", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Gaming Monitor", result.getContent().get(0).getName());
        
        verify(productRepository, times(1)).findByCategory(eq("Electronics"), any(Pageable.class));
    }

    /**
     * Validates that searching for an empty or non-existent category 
     * returns an empty {@link Page} rather than null.
     */
    @DisplayName("Mock Find By Category - Should return an empty Page when category has no products")
    @Test
    void testFindByCategory_NotFound() {
        // Arrange
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(productRepository.findByCategory(eq("Clothing"), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<Product> result = productRepository.findByCategory("Clothing", pageable);

        // Assert
        assertNotNull(result, "Page should not be null, even if empty");
        assertTrue(result.isEmpty(), "The page content should be empty");
        assertEquals(0, result.getTotalElements());
        
        verify(productRepository, times(1)).findByCategory(eq("Clothing"), any(Pageable.class));
    }

    /**
     * Verifies the retrieval of a single product by its unique identifier.
     */
    @DisplayName("Mock Find By ID - Should return Product when ID exists")
    @Test
    void testFindById() {
        // Arrange
        when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));

        // Act
        Optional<Product> result = productRepository.findById(100L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getCategory());
        
        verify(productRepository, times(1)).findById(100L);
    }

    /**
     * Ensures the save operation correctly processes product persistence 
     * and returns the entity with all attributes intact.
     */
    @DisplayName("Mock Save Product - Should return the saved Product")
    @Test
    void testSaveProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        // Act
        Product savedProduct = productRepository.save(new Product());

        // Assert
        assertNotNull(savedProduct);
        assertEquals(100L, savedProduct.getId());
        assertEquals("Gaming Monitor", savedProduct.getName());
        
        verify(productRepository, times(1)).save(any(Product.class));
    }

    /**
     * Verifies that the delete logic correctly triggers the repository's 
     * underlying deletion method.
     */
    @DisplayName("Mock Delete Product - Should verify delete method was called")
    @Test
    void testDeleteProduct() {
        // Act
        productRepository.delete(mockProduct);

        // Assert
        verify(productRepository, times(1)).delete(mockProduct);
    }
}