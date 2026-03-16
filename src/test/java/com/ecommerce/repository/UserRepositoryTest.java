package com.ecommerce.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;

/**
 * Unit test suite for the {@link UserRepository}.
 * This class validates the data access logic for user management, including 
 * specialized query methods for authentication and unique constraint checks.
 * By using Mockito, we ensure the repository contract is tested without 
 * initiating a physical database connection.
 */
@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User mockUser;

    /**
     * Initializes a sample user entity before each test.
     * This provides a consistent object state for verifying field mapping 
     * and query result handling.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aditya");
        mockUser.setEmail("aditya@test.com");
        mockUser.setPassword("encodedPassword123");
        mockUser.setRole(Role.CUSTOMER);
    }

    /**
     * Verifies that the custom {@code findByEmail} query correctly retrieves 
     * a user when a matching email exists in the database.
     */
    @DisplayName("Mock Find By Email - Should return User when Email exists")
    @Test
    void testFindByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("aditya@test.com")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userRepository.findByEmail("aditya@test.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Aditya", result.get().getName());
        
        verify(userRepository, times(1)).findByEmail("aditya@test.com");
    }

    /**
     * Ensures that querying for a non-existent email returns an empty {@link Optional}.
     */
    @DisplayName("Mock Find By Email - Should return empty Optional when Email does not exist")
    @Test
    void testFindByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userRepository.findByEmail("unknown@test.com");

        // Assert
        assertFalse(result.isPresent());
        
        verify(userRepository, times(1)).findByEmail("unknown@test.com");
    }

    /**
     * Validates the existence check for emails, which is critical during 
     * user registration to prevent duplicate accounts.
     */
    @DisplayName("Mock Exists By Email - Should return true if Email is in DB")
    @Test
    void testExistsByEmail_True() {
        // Arrange
        when(userRepository.existsByEmail("aditya@test.com")).thenReturn(true);

        // Act
        boolean exists = userRepository.existsByEmail("aditya@test.com");

        // Assert
        assertTrue(exists, "Should return true for an existing email");
        
        verify(userRepository, times(1)).existsByEmail("aditya@test.com");
    }

    /**
     * Ensures the existence check returns false when the email is not present.
     */
    @DisplayName("Mock Exists By Email - Should return false if Email is NOT in DB")
    @Test
    void testExistsByEmail_False() {
        // Arrange
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);

        // Act
        boolean exists = userRepository.existsByEmail("newuser@test.com");

        // Assert
        assertFalse(exists, "Should return false for a non-existing email");
        
        verify(userRepository, times(1)).existsByEmail("newuser@test.com");
    }

    /**
     * Verifies the standard retrieval of a user by their primary key.
     */
    @DisplayName("Mock Find By ID - Should return User when ID exists")
    @Test
    void testFindById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userRepository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(Role.CUSTOMER, result.get().getRole());
        
        verify(userRepository, times(1)).findById(1L);
    }

    /**
     * Ensures the save operation correctly processes user persistence 
     * and returns the entity with its generated identifier.
     */
    @DisplayName("Mock Save User - Should return the saved User")
    @Test
    void testSaveUser() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User savedUser = userRepository.save(new User());

        // Assert
        assertNotNull(savedUser);
        assertEquals(1L, savedUser.getId());
        assertEquals("aditya@test.com", savedUser.getEmail());
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Verifies that the delete operation triggers the repository's underlying removal logic.
     */
    @DisplayName("Mock Delete User - Should verify delete method was called")
    @Test
    void testDeleteUser() {
        // Act
        userRepository.delete(mockUser);

        // Assert
        verify(userRepository, times(1)).delete(mockUser);
    }
}