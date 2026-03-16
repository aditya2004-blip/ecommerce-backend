package com.ecommerce.serviceImplTest;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.service.ProductService;
import com.ecommerce.serviceImpl.CartServiceImpl;

/**
 * Unit test suite for {@link CartServiceImpl}.
 * This class validates the core business logic for shopping cart operations, 
 * including item addition, quantity updates, stock validation, and total 
 * price recalculations. 
 */
@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

	@Mock
    private CartRepository cartRepository;

    @Mock
    private ProductService productService;

    /**
     * The service instance under test with mocked dependencies.
     */
    @InjectMocks
    private CartServiceImpl cartService;

    private User mockUser;
    private Cart mockCart;
    private Product mockProduct;
    private CartItem mockCartItem;

    /**
     * Prepares a standardized e-commerce environment including a user, 
     * a product with a defined price, and an initial cart state.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);

        mockCart = new Cart();
        mockCart.setId(10L);
        mockCart.setUser(mockUser);
        mockCart.setCartItems(new ArrayList<>());
        mockCart.setTotalPrice(BigDecimal.ZERO);

        mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("Wireless Mouse");
        mockProduct.setPrice(new BigDecimal("50.00"));
        mockProduct.setStock(10); 

        mockCartItem = new CartItem();
        mockCartItem.setId(1000L);
        mockCartItem.setCart(mockCart);
        mockCartItem.setProduct(mockProduct);
        mockCartItem.setQuantity(1);
        mockCartItem.setPrice(new BigDecimal("50.00"));
    }
    
    /**
     * Verifies that the service successfully retrieves a cart for a valid user.
     */
    @DisplayName("Get Cart - Should return cart when valid user ID is provided")
    @Test
    void testGetCartByUserId_Success() {
    	when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
    	
    	Cart retrievedCart = cartService.getCartByUserId(1L);
    	
    	assertNotNull(retrievedCart);
    	assertEquals(10L, retrievedCart.getId());
    	verify(cartRepository, times(1)).findByUserId(1L);
    }

    /**
     * Ensures a ResourceNotFoundException is thrown when a cart does not exist for a user.
     */
    @DisplayName("Get Cart - Should throw exception when user ID has no cart")
    @Test
    void testGetCartByUserId_NotFound_ThrowsException() {
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCartByUserId(99L);
        });
    }
    
    /**
     * Business Rule Test: Verifies that a user cannot add more items to the 
     * cart than what is currently available in the warehouse stock.
     */
    @DisplayName("Add Item - Should throw exception if requested quantity exceeds product stock")
    @Test
    void testAddOrUpdateCartItem_NotEnoughStock_ThrowsException() {
    	when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
    	when(productService.getProductById(100L)).thenReturn(mockProduct);
    	
    	assertThrows(BadRequestException.class, () -> {
    		cartService.addOrUpdateCartItem(1L, 100L, 15); // Exceeds stock of 10
    	});
    	
    	verify(cartRepository, never()).save(any(Cart.class));
    }
    
    /**
     * Verifies that adding an existing product updates the quantity of the 
     * specific CartItem rather than creating a duplicate entry.
     */
    @DisplayName("Update Item - Should update quantity and recalculate total if product is already in cart")
    @Test
    void testAddOrUpdateCartItem_ExistingItem_UpdatesQuantityAndTotal() {
        mockCart.getCartItems().add(mockCartItem);
        
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
        when(productService.getProductById(100L)).thenReturn(mockProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        Cart updatedCart = cartService.addOrUpdateCartItem(1L, 100L, 3);

        assertEquals(1, updatedCart.getCartItems().size(), "Should not create a duplicate item");
        assertEquals(3, updatedCart.getCartItems().get(0).getQuantity(), "Quantity should be updated");
        assertEquals(new BigDecimal("150.00"), updatedCart.getTotalPrice(), "Total price should be recalculated");
    }
    
    /**
     * Tests the creation of a new CartItem and verifies the initial price calculation.
     */
    @DisplayName("Add Item - Should create new CartItem and recalculate total if product is not in cart")
    @Test
    void testAddOrUpdateCartItem_NewItem_AddsToCartAndCalculatesTotal() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
        when(productService.getProductById(100L)).thenReturn(mockProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        Cart updatedCart = cartService.addOrUpdateCartItem(1L, 100L, 2);

        assertEquals(1, updatedCart.getCartItems().size(), "New item should be added to the list");
        assertEquals(2, updatedCart.getCartItems().get(0).getQuantity());
        assertEquals(new BigDecimal("100.00"), updatedCart.getTotalPrice(), "Total price should be recalculated");
    }
    
    /**
     * Verifies that removing an item reduces the cart's total price 
     * back to its base state.
     */
    @DisplayName("Remove Item - Should remove product from cart and recalculate total")
    @Test
    void testRemoveProductFromCart_Success() {
        mockCart.getCartItems().add(mockCartItem);
        mockCart.setTotalPrice(new BigDecimal("50.00"));

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(mockCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        Cart updatedCart = cartService.removeProductFromCart(1L, 100L);

        assertTrue(updatedCart.getCartItems().isEmpty(), "Cart should be empty after removal");
        assertEquals(BigDecimal.ZERO, updatedCart.getTotalPrice(), "Total price should reset to zero");
    }
    
    /**
     * Validates the "clear cart" logic, ensuring all associated items 
     * are purged and the financial total is reset.
     */
    @DisplayName("Clear Cart - Should empty all items and set total to zero")
    @Test
    void testClearCart_Success() {
        mockCart.getCartItems().add(mockCartItem);
        mockCart.setTotalPrice(new BigDecimal("50.00"));
        
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

        cartService.clearCart(mockCart);

        assertTrue(mockCart.getCartItems().isEmpty(), "All items should be cleared");
        assertEquals(BigDecimal.ZERO, mockCart.getTotalPrice(), "Total price should be strictly zero");
        verify(cartRepository, times(1)).save(mockCart);
    }
}