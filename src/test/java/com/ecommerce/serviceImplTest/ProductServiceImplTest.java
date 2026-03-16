package com.ecommerce.serviceImplTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.serviceImpl.ProductServiceImpl;
import com.ecommerce.serviceImpl.RedisInventoryServiceImpl;

/**
 * Unit test suite for {@link ProductServiceImpl}.
 * This class validates the business logic for catalog management, including 
 * CRUD operations and inventory synchronization. It ensures that any change 
 * to product stock in the database is correctly reflected in the 
 * high-performance Redis cache.
 */
@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private RedisInventoryServiceImpl redisInventoryService;

    /**
     * The service instance under test with mocked dependencies.
     */
    @InjectMocks
    private ProductServiceImpl productService;

    private Product existingProduct;
    private Product updatedDetails;
    private Page<Product> mockPage;

    /**
     * Prepares standardized product data and mock paginated responses 
     * before each test execution.
     */
    @BeforeEach
    void setUp() {
        existingProduct = new Product();
        existingProduct.setId(100L);
        existingProduct.setName("Smartphone");
        existingProduct.setDescription("A very smart phone");
        existingProduct.setPrice(new BigDecimal("699.99"));
        existingProduct.setStock(50);
        existingProduct.setCategory("Electronics");
        existingProduct.setImageUrl("http://image.url/phone.jpg");

        updatedDetails = new Product();
        updatedDetails.setName("Updated Smartphone");
        updatedDetails.setDescription("Even smarter phone");
        updatedDetails.setPrice(new BigDecimal("599.99"));
        updatedDetails.setStock(40);
        updatedDetails.setCategory("Electronics");
        updatedDetails.setImageUrl("http://image.url/newphone.jpg");

        mockPage = new PageImpl<>(List.of(existingProduct));
    }

    /**
     * Verifies that adding a product persists it to the database AND 
     * initializes the stock level in the Redis cache.
     */
    @DisplayName("Add Product - Should successfully save and return product")
    @Test
    void testAddProduct_Success() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product savedProduct = productService.addProduct(existingProduct);

        // Assert
        assertNotNull(savedProduct);
        assertEquals("Smartphone", savedProduct.getName());
        verify(productRepository, times(1)).save(existingProduct);
        
        // Ensure cache synchronization logic was triggered
        verify(redisInventoryService, times(1)).setStock(existingProduct.getId(), existingProduct.getStock());
    }

    /**
     * Verifies retrieval of a single product when a valid ID is provided.
     */
    @DisplayName("Get Product - Should return product when valid ID is provided")
    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(existingProduct));

        Product retrievedProduct = productService.getProductById(100L);

        assertNotNull(retrievedProduct);
        assertEquals(100L, retrievedProduct.getId());
    }

    /**
     * Ensures that querying for a non-existent product ID results in the 
     * appropriate custom exception.
     */
    @DisplayName("Get Product - Should throw exception when ID does not exist")
    @Test
    void testGetProductById_NotFound_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });
    }

    /**
     * Verifies that updating a product correctly modifies all fields, 
     * persists the changes, and refreshes the Redis cache with the new stock count.
     */
    @DisplayName("Update Product - Should successfully overwrite all fields and save")
    @Test
    void testUpdateProduct_Success() {
        // Arrange
        when(productRepository.findById(100L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct); 

        // Act
        Product result = productService.updateProduct(100L, updatedDetails);

        // Assert
        assertEquals("Updated Smartphone", result.getName());
        assertEquals(new BigDecimal("599.99"), result.getPrice());
        assertEquals(40, result.getStock());

        verify(productRepository, times(1)).save(existingProduct);
        
        // Ensure Redis is updated after a product modification
        verify(redisInventoryService, times(1)).setStock(100L, 40);
    }

    /**
     * Validates that the delete operation triggers the repository removal 
     * logic for an existing product.
     */
    @DisplayName("Delete Product - Should successfully delete when product exists")
    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(existingProduct));

        productService.deleteProduct(100L);

        verify(productRepository, times(1)).delete(existingProduct);
    }

    /**
     * Verifies the paginated retrieval logic when no category filter is applied.
     */
    @DisplayName("Get All Products - Should return unfiltered page when category is null")
    @Test
    void testGetAllProducts_NullCategory_ReturnsAll() {
        Pageable expectedPageable = PageRequest.of(0, 10);
        when(productRepository.findAll(expectedPageable)).thenReturn(mockPage);

        Page<Product> result = productService.getAllProducts(0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findAll(expectedPageable);
        verify(productRepository, never()).findByCategory(anyString(), any(Pageable.class));
    }

    /**
     * Verifies the paginated retrieval logic when a specific category filter is used.
     */
    @DisplayName("Get All Products - Should return filtered page when category is provided")
    @Test
    void testGetAllProducts_WithCategory_ReturnsFiltered() {
        Pageable expectedPageable = PageRequest.of(0, 10);
        when(productRepository.findByCategory(eq("Electronics"), eq(expectedPageable))).thenReturn(mockPage);

        Page<Product> result = productService.getAllProducts(0, 10, "Electronics");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findByCategory(eq("Electronics"), eq(expectedPageable));
        verify(productRepository, never()).findAll(any(Pageable.class));
    }
}