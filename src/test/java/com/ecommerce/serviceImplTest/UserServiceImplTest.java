package com.ecommerce.serviceImplTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.serviceImpl.UserServiceImpl;

/**
 * Unit test suite for {@link UserServiceImpl}.
 * This class validates the management logic for user profiles, including 
 * profile retrieval, conditional updates (patching), and secure deletion. 
 * It ensures that sensitive fields like passwords are only re-encoded 
 * when provided and that non-existent resources trigger appropriate exceptions.
 */
@ExtendWith(MockitoExtension.class) 
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * The service under test with injected mocks for persistence and security.
     */
    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;
    private User updatedDetails;

    /**
     * Prepares a baseline user and an empty container for update details 
     * before each test execution.
     */
    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@test.com");
        existingUser.setPassword("oldEncodedPassword");
        existingUser.setRole(Role.CUSTOMER);

        updatedDetails = new User();
    }

    /**
     * Verifies that the service retrieves a user profile correctly by its primary key.
     */
    @DisplayName("Get User - Should return user when valid ID is provided")
    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // Act
        User retrievedUser = userService.getUserById(1L);

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(1L, retrievedUser.getId());
        assertEquals("Old Name", retrievedUser.getName());
        verify(userRepository, times(1)).findById(1L);
    }

    /**
     * Ensures that attempting to fetch a non-existent user profile 
     * results in a ResourceNotFoundException.
     */
    @DisplayName("Get User - Should throw exception when user ID does not exist")
    @Test
    void testGetUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(99L);
        });
    }

    /**
     * Tests a full profile update.
     * Verifies that all fields, including the encrypted password, 
     * are correctly modified and persisted.
     */
    @DisplayName("Update User - Should update all fields when full payload is provided")
    @Test
    void testUpdateUser_FullUpdate_Success() {
        // Arrange
        updatedDetails.setName("New Name");
        updatedDetails.setEmail("new@test.com");
        updatedDetails.setRole(Role.ADMIN);
        updatedDetails.setPassword("newRawPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newRawPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        User result = userService.updateUser(1L, updatedDetails);

        // Assert
        assertEquals("New Name", result.getName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());
        assertEquals("newEncodedPassword", result.getPassword()); 

        verify(userRepository, times(1)).save(existingUser);
    }

    /**
     * Tests the "Partial Update" or "Patch" logic.
     * Verifies that if only one field (name) is provided, other existing fields 
     * (email, role, password) remain untouched.
     */
    @DisplayName("Update User - Should only update provided fields and ignore nulls")
    @Test
    void testUpdateUser_PartialUpdate_Success() {
        // Arrange
        updatedDetails.setName("Only Name Changed");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        User result = userService.updateUser(1L, updatedDetails);

        // Assert
        assertEquals("Only Name Changed", result.getName());
        assertEquals("old@test.com", result.getEmail(), "Email should not change");
        assertEquals(Role.CUSTOMER, result.getRole(), "Role should not change");
        assertEquals("oldEncodedPassword", result.getPassword(), "Password should not change");

        // Ensure the encoder was never called since no password was provided
        verify(passwordEncoder, never()).encode(anyString()); 
        verify(userRepository, times(1)).save(existingUser);
    }

    /**
     * Verifies that an empty password string is treated as "no update," 
     * preventing the accidental clearing or re-encoding of empty values.
     */
    @DisplayName("Update User - Should not update password if password string is empty")
    @Test
    void testUpdateUser_EmptyPassword_IgnoresPasswordUpdate() {
        updatedDetails.setPassword("");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(1L, updatedDetails);

        assertEquals("oldEncodedPassword", result.getPassword());
        verify(passwordEncoder, never()).encode(anyString()); 
    }

    /**
     * Verifies that a ResourceNotFoundException is thrown if the target 
     * user for an update does not exist.
     */
    @DisplayName("Update User - Should throw exception if user does not exist")
    @Test
    void testUpdateUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(99L, updatedDetails);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Validates that the delete operation correctly triggers the 
     * removal of an existing user profile.
     */
    @DisplayName("Delete User - Should successfully delete when user exists")
    @Test
    void testDeleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).delete(existingUser);
    }

    /**
     * Verifies that attempting to delete a non-existent user results 
     * in a ResourceNotFoundException.
     */
    @DisplayName("Delete User - Should throw exception if user to delete does not exist")
    @Test
    void testDeleteUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(99L);
        });

        verify(userRepository, never()).delete(any(User.class));
    }
}