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

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;

/**
 * Unit test for the {@link OrderItemRepository}.
 * This class validates the persistence operations for individual order line items.
 * By using Mockito, we verify that the relationship between Orders, Products, and 
 * their respective OrderItems is correctly handled at the data access level.
 */
@ExtendWith(MockitoExtension.class) 
public class OrderItemRepositoryTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    private OrderItem mockOrderItem;
    private Order mockOrder;
    private Product mockProduct;

    /**
     * Initializes the testing context by building a mock relationship between 
     * an Order, a Product, and the resulting OrderItem.
     */
    @BeforeEach
    void setUp() {
        mockOrder = new Order();
        mockOrder.setId(500L);
        mockOrder.setTotalAmount(new BigDecimal("150.00"));

        mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("Mechanical Keyboard");
        mockProduct.setPrice(new BigDecimal("75.00"));

        mockOrderItem = new OrderItem();
        mockOrderItem.setId(5000L);
        mockOrderItem.setOrder(mockOrder);
        mockOrderItem.setProduct(mockProduct);
        mockOrderItem.setQuantity(2);
        mockOrderItem.setPrice(new BigDecimal("150.00")); 
    }

    /**
     * Verifies that the save operation correctly persists an OrderItem and 
     * returns the object with its assigned metadata.
     */
    @DisplayName("Mock Save - Should return the saved OrderItem")
    @Test
    void testSaveOrderItem() {
        // Arrange
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(mockOrderItem);

        // Act
        OrderItem savedItem = orderItemRepository.save(new OrderItem());

        // Assert
        assertNotNull(savedItem);
        assertEquals(5000L, savedItem.getId());
        assertEquals(2, savedItem.getQuantity());
        assertEquals(new BigDecimal("150.00"), savedItem.getPrice());
        
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    /**
     * Verifies the retrieval of an OrderItem by its ID.
     * Ensures that linked entities (Product and Order) are correctly 
     * accessible from the retrieved item.
     */
    @DisplayName("Mock Find - Should return OrderItem if ID exists")
    @Test
    void testFindById() {
        // Arrange
        when(orderItemRepository.findById(5000L)).thenReturn(Optional.of(mockOrderItem));

        // Act
        Optional<OrderItem> result = orderItemRepository.findById(5000L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Mechanical Keyboard", result.get().getProduct().getName());
        assertEquals(500L, result.get().getOrder().getId(), "The linked order ID should match");
        
        verify(orderItemRepository, times(1)).findById(5000L);
    }

    /**
     * Verifies that the deletion logic correctly invokes the repository's delete method.
     */
    @DisplayName("Mock Delete - Should verify delete method was called")
    @Test
    void testDeleteOrderItem() {
        // Act
        orderItemRepository.delete(mockOrderItem);

        // Assert
        verify(orderItemRepository, times(1)).delete(mockOrderItem);
    }
}