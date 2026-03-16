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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ecommerce.dto.CartDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.service.CartService;

/**
 * Unit test suite for the {@link CartController}.
 * This class validates the RESTful endpoints for shopping cart management, ensuring 
 * correct HTTP status codes are returned and that the ModelMapper correctly 
 * transforms internal entities into public-facing DTOs.
 */
@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @Mock
    private ModelMapper modelMapper;

    /**
     * The controller under test with mocked service and mapper injected.
     */
    @InjectMocks
    private CartController cartController;

    private Cart mockCart;
    private CartDto mockCartDto;

    /**
     * Initializes the MockMvc standalone context and sets up reusable mock data 
     * before each test execution.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();

        mockCart = new Cart();
        mockCart.setId(10L);
        mockCart.setTotalPrice(new BigDecimal("150.00"));

        mockCartDto = new CartDto();
        mockCartDto.setId(10L);
        mockCartDto.setTotalPrice(new BigDecimal("150.00"));
    }

    /**
     * Verifies that the GET endpoint correctly retrieves a user's cart and returns 
     * a serialized DTO.
     */
    @DisplayName("GET /api/cart - Should return 200 OK and the mapped CartDto")
    @Test
    void testGetCart_Success() throws Exception {
        // Arrange
        when(cartService.getCartByUserId(1L)).thenReturn(mockCart);
        when(modelMapper.map(any(Cart.class), eq(CartDto.class))).thenReturn(mockCartDto);

        // Act & Assert
        mockMvc.perform(get("/api/cart?userId=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.totalPrice").value(150.00));

        verify(cartService, times(1)).getCartByUserId(1L);
    }

    /**
     * Verifies the POST endpoint for adding items to the cart.
     * Ensures parameters (userId, productId, quantity) are correctly passed to the service.
     */
    @DisplayName("POST /api/cart/add/{productId} - Should return 200 OK and mapped CartDto")
    @Test
    void testAddToCart_Success() throws Exception {
        // Arrange
        when(cartService.addOrUpdateCartItem(1L, 100L, 2)).thenReturn(mockCart);
        when(modelMapper.map(any(Cart.class), eq(CartDto.class))).thenReturn(mockCartDto);

        // Act & Assert
        mockMvc.perform(post("/api/cart/add/100?userId=1&quantity=2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(cartService, times(1)).addOrUpdateCartItem(1L, 100L, 2);
    }

    /**
     * Verifies the PUT endpoint for updating item quantities.
     */
    @DisplayName("PUT /api/cart/update/{productId} - Should return 200 OK and raw Cart entity")
    @Test
    void testUpdateCartItem_Success() throws Exception {
        // Arrange
        when(cartService.addOrUpdateCartItem(1L, 100L, 5)).thenReturn(mockCart);

        // Act & Assert
        mockMvc.perform(put("/api/cart/update/100?userId=1&quantity=5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(cartService, times(1)).addOrUpdateCartItem(1L, 100L, 5);
    }

    /**
     * Verifies the DELETE endpoint for removing a product from the cart.
     */
    @DisplayName("DELETE /api/cart/remove/{productId} - Should return 200 OK and raw Cart entity")
    @Test
    void testRemoveFromCart_Success() throws Exception {
        // Arrange
        when(cartService.removeProductFromCart(1L, 100L)).thenReturn(mockCart);

        // Act & Assert
        mockMvc.perform(delete("/api/cart/remove/100?userId=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(cartService, times(1)).removeProductFromCart(1L, 100L);
    }
}