package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test suite for {@link ProductController}.
 * This class validates the product catalog API, including paginated retrieval, 
 * filtering by category, and administrative CRUD operations. It ensures that 
 * the controller correctly interacts with the {@link ProductService} and 
 * maps internal entities to {@link ProductDto} for the API response.
 */
@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private ModelMapper modelMapper;

    /**
     * The controller under test with mocked dependencies injected.
     */
    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper;

    private Product mockProduct;
    private ProductDto mockProductDto;
    private Page<Product> mockProductPage;

    /**
     * Initializes the MockMvc standalone environment and pre-loads 
     * common product test data before each test execution.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();

        mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("Gaming Mouse");
        mockProduct.setPrice(new BigDecimal("50.00"));
        mockProduct.setCategory("Electronics");

        mockProductDto = new ProductDto();
        mockProductDto.setId(100L);
        mockProductDto.setName("Gaming Mouse");
        mockProductDto.setPrice(new BigDecimal("50.00"));
        mockProductDto.setCategory("Electronics");

        // Simulate a paginated response from the service
        mockProductPage = new PageImpl<>(List.of(mockProduct), org.springframework.data.domain.PageRequest.of(0, 10), 1);
    }

    /**
     * Tests paginated product retrieval with category filtering.
     * Verifies that the JSON response follows the Spring Data Page structure.
     */
    @DisplayName("GET /api/products - Should return 200 OK and a Page of ProductDtos")
    @Test
    void testGetAllProducts_Success() throws Exception {
        when(productService.getAllProducts(0, 10, "Electronics")).thenReturn(mockProductPage);
        when(modelMapper.map(any(), eq(ProductDto.class))).thenReturn(mockProductDto);

        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10")
                .param("category", "Electronics")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andDo(print()) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].name").value("Gaming Mouse"));

        verify(productService, times(1)).getAllProducts(0, 10, "Electronics");
    }

    /**
     * Verifies retrieval of a single product's details by ID.
     */
    @DisplayName("GET /api/products/{id} - Should return 200 OK and mapped ProductDto")
    @Test
    void testGetProductById_Success() throws Exception {
        when(productService.getProductById(100L)).thenReturn(mockProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDto.class))).thenReturn(mockProductDto);

        mockMvc.perform(get("/api/products/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
        
        verify(productService, times(1)).getProductById(100L);
    }

    /**
     * Tests the creation of a new product record.
     * Ensures the response status is 201 Created.
     */
    @DisplayName("POST /api/products - Should return 201 Created and mapped ProductDto")
    @Test
    void testAddProduct_Success() throws Exception {
        when(productService.addProduct(any(Product.class))).thenReturn(mockProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDto.class))).thenReturn(mockProductDto);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockProduct))) 
                
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));

        verify(productService, times(1)).addProduct(any(Product.class));
    }

    /**
     * Tests updating an existing product's information.
     */
    @DisplayName("PUT /api/products/{id} - Should return 200 OK and mapped ProductDto")
    @Test
    void testUpdateProduct_Success() throws Exception {
        when(productService.updateProduct(eq(100L), any(Product.class))).thenReturn(mockProduct);
        when(modelMapper.map(any(Product.class), eq(ProductDto.class))).thenReturn(mockProductDto);

        mockMvc.perform(put("/api/products/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockProduct)))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        verify(productService, times(1)).updateProduct(eq(100L), any(Product.class));
    }

    /**
     * Tests the deletion of a product and ensures the correct plain-text confirmation is returned.
     */
    @DisplayName("DELETE /api/products/{id} - Should return 200 OK and success string")
    @Test
    void testDeleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/products/100")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("Product deleted"));

        verify(productService, times(1)).deleteProduct(100L);
    }
}