package com.ecommerce.controller;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for managing product catalog operations.
 * Handles public viewing of products and administrative CRUD operations.
 * * Features:
 * - Role-Based Access Control (RBAC): Public catalog browsing vs Admin-only modifications.
 * - Server-side Pagination & Filtering.
 * - Optimistic Locking support for data integrity.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "2. Product API", description = "Endpoints for browsing, searching, and managing the product inventory.")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ModelMapper modelMapper;

    /**
     * Constructor-based dependency injection for core services and mapping utilities.
     */
    @Autowired
    public ProductController(ProductService productService, ModelMapper modelMapper) {
        this.productService = productService;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves a paginated list of products from the catalog.
     * Optionally filters by product category if provided.
     * * @param page Zero-based page index.
     * @param size Number of records per page.
     * @param category Optional category name for filtering.
     * @return ResponseEntity containing a Page of ProductDto objects.
     */
    @Operation(
        summary = "Get All Products (Paginated)", 
        description = "Retrieves a paginated list of products. Can be filtered by category name. This endpoint is **Publicly accessible**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product list")
    })
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, 
            @RequestParam(required = false) String category) {
        
        logger.info("Received GET request at /api/products. Page: {}, Size: {}, Category: {}", page, size,
                (category != null ? category : "NONE"));

        // Fetch paginated entity list from service
        Page<Product> products = productService.getAllProducts(page, size, category);
        
        // Map entity Page to DTO Page to hide internal database structure
        Page<ProductDto> productDtos = products.map(product -> modelMapper.map(product, ProductDto.class));

        logger.info("Successfully retrieved {} products for page {}. Returning 200 OK.", products.getNumberOfElements(), page);
        return ResponseEntity.ok(productDtos);
    }

    /**
     * Fetches detailed information for a single product by its unique identifier.
     * * @param id The unique identifier of the product.
     * @return ResponseEntity containing the requested ProductDto.
     */
    @Operation(
        summary = "Get Specific Product by ID", 
        description = "Fetches detailed information for a single product record. This endpoint is **Publicly accessible**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the product"),
        @ApiResponse(responseCode = "404", description = "Product not found with the provided ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        logger.info("Received GET request at /api/products/{} to fetch product details.", id);
        
        Product product = productService.getProductById(id);
        
        logger.info("Successfully fetched details for Product ID: {}. Returning 200 OK.", id);
        return ResponseEntity.ok(modelMapper.map(product, ProductDto.class));
    }

    /**
     * Persists a new product into the database.
     * Restricted strictly to users with 'ADMIN' authority.
     * * @param product The product entity to be created (validated via @Valid).
     * @return ResponseEntity containing the saved ProductDto and 201 Created status.
     */
    @Operation(
        summary = "Add New Product", 
        description = "Adds a new product to the database. Requires **ADMIN** role. Implements **Optimistic Locking** via versioning.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product successfully created"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions (ADMIN role required)"),
        @ApiResponse(responseCode = "400", description = "Invalid product data provided")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductDto> addProduct(@Valid @RequestBody Product product) {
        logger.info("Received POST request at /api/products to add a new product: {}", product.getName());
        
        Product savedProduct = productService.addProduct(product);
        
        logger.info("Successfully created product with ID: {}. Returning 201 CREATED.", savedProduct.getId());
        return new ResponseEntity<>(modelMapper.map(savedProduct, ProductDto.class), HttpStatus.CREATED);
    }

    /**
     * Updates an existing product's information.
     * Restricted to 'ADMIN' authority. Uses Optimistic Locking to handle concurrent updates.
     * * @param id The ID of the product to update.
     * @param product The updated product details.
     * @return ResponseEntity containing the updated ProductDto.
     */
    @Operation(
        summary = "Update Existing Product", 
        description = "Updates an existing product's details. Requires **ADMIN** role. Checks **Optimistic Locking** version to prevent race conditions.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product successfully updated"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "409", description = "Conflict - Version mismatch (Race condition)")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        logger.info("Received PUT request at /api/products/{} to update product details.", id);
        
        Product updatedProduct = productService.updateProduct(id, product);
        
        logger.info("Successfully updated product with ID: {}. Returning 200 OK.", id);
        return ResponseEntity.ok(modelMapper.map(updatedProduct, ProductDto.class));
    }

    /**
     * Removes a product from the catalog permanently.
     * Restricted strictly to users with 'ADMIN' authority.
     * * @param id The ID of the product to delete.
     * @return ResponseEntity with a success message.
     */
    @Operation(
        summary = "Delete Product", 
        description = "Permanently removes a product from the catalog. Requires **ADMIN** role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        logger.warn("Security Event: Received DELETE request at /api/products/{} to remove product.", id);
        
        productService.deleteProduct(id);
        
        logger.info("Successfully deleted Product ID: {}. Returning 200 OK.", id);
        return ResponseEntity.ok("Product deleted");
    }
}