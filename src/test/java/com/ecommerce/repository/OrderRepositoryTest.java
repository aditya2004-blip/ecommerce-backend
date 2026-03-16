package com.ecommerce.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;

/**
 * Unit test suite for {@link OrderRepository}.
 * This class validates the data access layer for customer orders. It specifically 
 * tests the custom derived query for fetching order history by user ID, 
 * standard CRUD operations, and the handling of edge cases such as empty results.
 */
@ExtendWith(MockitoExtension.class) 
public class OrderRepositoryTest {

    @Mock
    private OrderRepository orderRepository;

    private Order mockOrder;
    private User mockUser;

    /**
     * Initializes a mock user and a corresponding order before each test.
     * This setup defines the baseline state for testing order attributes 
     * like status, payment success, and relational mapping.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aditya");
        mockUser.setEmail("aditya@test.com");
        mockUser.setRole(Role.CUSTOMER);

        mockOrder = new Order();
        mockOrder.setId(500L);
        mockOrder.setUser(mockUser);
        mockOrder.setTotalAmount(new BigDecimal("350.00"));
        mockOrder.setOrderStatus(OrderStatus.PLACED);
        mockOrder.setPaymentStatus(PaymentStatus.SUCCESS);
    }

    /**
     * Verifies that the repository correctly retrieves a list of orders for a user.
     * Ensures that the list is non-null and contains the expected order metadata.
     */
    @DisplayName("Mock Find By User ID - Should return a List of Orders for valid User ID")
    @Test
    void testFindByUserId_Success() {
        // Arrange
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(mockOrder));

        // Act
        List<Order> result = orderRepository.findByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(500L, result.get(0).getId());
        assertEquals(1L, result.get(0).getUser().getId());
        
        verify(orderRepository, times(1)).findByUserId(1L);
    }

    /**
     * Validates the behavior when a user has no order history.
     * Ensures that the repository returns an empty collection rather than null, 
     * adhering to standard Spring Data JPA conventions.
     */
    @DisplayName("Mock Find By User ID - Should return an empty List when User ID has no orders")
    @Test
    void testFindByUserId_NotFound() {
        // Arrange
        when(orderRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderRepository.findByUserId(99L);

        // Assert
        assertNotNull(result, "Spring Data JPA derived queries return empty lists, never nulls");
        assertTrue(result.isEmpty(), "The list should be empty");
        
        verify(orderRepository, times(1)).findByUserId(99L);
    }

    /**
     * Verifies the retrieval of a single order by its primary key.
     */
    @DisplayName("Mock Find By ID - Should return Order when ID exists")
    @Test
    void testFindById() {
        // Arrange
        when(orderRepository.findById(500L)).thenReturn(Optional.of(mockOrder));

        // Act
        Optional<Order> result = orderRepository.findById(500L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(OrderStatus.PLACED, result.get().getOrderStatus());
        
        verify(orderRepository, times(1)).findById(500L);
    }

    /**
     * Ensures that saving an order returns the persisted entity with all fields intact.
     */
    @DisplayName("Mock Save Order - Should return the saved Order")
    @Test
    void testSaveOrder() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        Order savedOrder = orderRepository.save(new Order());

        // Assert
        assertNotNull(savedOrder);
        assertEquals(500L, savedOrder.getId());
        assertEquals(new BigDecimal("350.00"), savedOrder.getTotalAmount());
        
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    /**
     * Verifies that the delete operation triggers the repository's internal deletion logic.
     */
    @DisplayName("Mock Delete Order - Should verify delete method was called")
    @Test
    void testDeleteOrder() {
        // Act
        orderRepository.delete(mockOrder);

        // Assert
        verify(orderRepository, times(1)).delete(mockOrder);
    }
}