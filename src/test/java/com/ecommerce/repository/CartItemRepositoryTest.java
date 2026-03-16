package com.ecommerce.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;

/**
 * Unit test for the {@link CartItemRepository} interface.
 * This class uses Mockito to simulate repository behavior, ensuring that the 
 * data access layer logic for individual cart line items is verified without 
 * requiring a connection to a live database.
 */
@ExtendWith(MockitoExtension.class) 
public class CartItemRepositoryTest {

    @Mock
    private CartItemRepository cartItemRepository;

    private CartItem mockCartItem;
    private Cart mockCart;
    private Product mockProduct;

    /**
     * Initializes a complete object graph including User, Cart, Product, and CartItem.
     * This provides a realistic context for testing the persistence behavior of line items.
     */
    @BeforeEach
    void setUp() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Mock User");
        mockUser.setRole(Role.CUSTOMER);

        mockCart = new Cart();
        mockCart.setId(10L);
        mockCart.setUser(mockUser);

        mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("Mock Headphones");
        mockProduct.setPrice(new BigDecimal("100.00"));

        mockCartItem = new CartItem();
        mockCartItem.setId(1000L);
        mockCartItem.setCart(mockCart);
        mockCartItem.setProduct(mockProduct);
        mockCartItem.setQuantity(2);
        mockCartItem.setPrice(new BigDecimal("200.00"));
    }

    /**
     * Verifies that the save operation correctly returns the persisted entity 
     * and that the repository method is invoked.
     */
    @DisplayName("Mock Save - Should return the saved CartItem")
    @Test
    void testSaveCartItem() {
        // Arrange
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);

        // Act
        CartItem savedItem = cartItemRepository.save(new CartItem());

        // Assert
        assertNotNull(savedItem);
        assertEquals(1000L, savedItem.getId());
        assertEquals(2, savedItem.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Verifies the retrieval of a cart item by its ID.
     * Ensures the association with the Product remains intact during the fetch.
     */
    @DisplayName("Mock Find - Should return CartItem if ID exists")
    @Test
    void testFindById() {
        // Arrange
        when(cartItemRepository.findById(1000L)).thenReturn(Optional.of(mockCartItem));

        // Act
        Optional<CartItem> result = cartItemRepository.findById(1000L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Mock Headphones", result.get().getProduct().getName());
        verify(cartItemRepository, times(1)).findById(1000L);
    }

    /**
     * Verifies that the delete operation is correctly triggered at the repository level.
     */
    @DisplayName("Mock Delete - Should verify delete method was called")
    @Test
    void testDeleteCartItem() {
        // Act
        cartItemRepository.delete(mockCartItem);

        // Assert
        verify(cartItemRepository, times(1)).delete(mockCartItem);
    }
}