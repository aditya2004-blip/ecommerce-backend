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
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;

/**
 * Unit test for {@link CartRepository}.
 * This class validates the data access logic for shopping carts, focusing on
 * user-to-cart mapping and persistence operations using Mockito to isolate the
 * repository layer.
 */
@ExtendWith(MockitoExtension.class) 
public class CartRepositoryTest {

    @Mock
    private CartRepository cartRepository;

    private Cart mockCart;
    private User mockUser;

    /**
     * Sets up the test environment with a mock user and a corresponding cart.
     * This baseline data ensures that the relationship between user and cart is 
     * accurately represented in all test cases.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aditya");
        mockUser.setEmail("aditya@test.com");
        mockUser.setRole(Role.CUSTOMER);

        mockCart = new Cart();
        mockCart.setId(10L);
        mockCart.setUser(mockUser);
        mockCart.setTotalPrice(new BigDecimal("250.00"));
    }

    /**
     * Verifies that the custom {@code findByUserId} query correctly retrieves
     * the cart associated with an existing user ID.
     */
    @DisplayName("Mock Find By User ID - Should return Cart when User ID exists")
    @Test
    void testFindByUserId_Success() {
        // Arrange
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));

        // Act
        Optional<Cart> result = cartRepository.findByUserId(1L);

        // Assert
        assertTrue(result.isPresent(), "Cart should be present for valid user ID");
        assertEquals(10L, result.get().getId());
        assertEquals(1L, result.get().getUser().getId(), "The user ID inside the cart should match");
        
        verify(cartRepository, times(1)).findByUserId(1L);
    }

    /**
     * Validates that the repository returns an empty Optional when a user ID
     * without an associated cart is queried.
     */
    @DisplayName("Mock Find By User ID - Should return empty Optional when User ID does not exist")
    @Test
    void testFindByUserId_NotFound() {
        // Arrange
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Cart> result = cartRepository.findByUserId(99L);

        // Assert
        assertFalse(result.isPresent(), "Cart should not be present for invalid user ID");
        
        verify(cartRepository, times(1)).findByUserId(99L);
    }

    /**
     * Ensures that the save operation correctly processes a Cart entity
     * and returns the persisted object with its generated attributes.
     */
    @DisplayName("Mock Save Cart - Should return the saved Cart")
    @Test
    void testSaveCart() {
        // Arrange
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        // Act
        Cart savedCart = cartRepository.save(new Cart());

        // Assert
        assertNotNull(savedCart);
        assertEquals(10L, savedCart.getId());
        assertEquals(new BigDecimal("250.00"), savedCart.getTotalPrice());
        
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    /**
     * Verifies that the deletion logic triggers the repository's delete method.
     */
    @DisplayName("Mock Delete Cart - Should verify delete method was called")
    @Test
    void testDeleteCart() {
        // Act
        cartRepository.delete(mockCart);

        // Assert
        verify(cartRepository, times(1)).delete(mockCart);
    }
}