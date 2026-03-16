package com.ecommerce.serviceImplTest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.serviceImpl.CustomUserDetails;
import com.ecommerce.serviceImpl.CustomUserDetailsService;

/**
 * Unit test for {@link CustomUserDetailsService}.
 * This class verifies the bridge between the application's user database 
 * and Spring Security's authentication mechanism. It ensures that 
 * user entities are correctly transformed into {@link CustomUserDetails} 
 * objects with appropriate roles and identifiers.
 */
@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

	@Mock
    private UserRepository userRepository;

    /**
     * The service under test with the mocked repository injected.
     */
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User mockUser;

    /**
     * Sets up a mock user with an ADMIN role for testing the 
     * mapping of security authorities.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("admin@test.com");
        mockUser.setPassword("encodedPassword123");
        mockUser.setRole(Role.ADMIN);
    }
    
    /**
     * Verifies that the service correctly retrieves a user by email and 
     * populates a CustomUserDetails object with all necessary security data.
     * * This test specifically checks for:
     * 1. The custom User ID (critical for internal session tracking).
     * 2. Username/Password mapping.
     * 3. Role-to-Authority mapping using the {@link SimpleGrantedAuthority} pattern.
     */
    @DisplayName("Load User - Should successfully map CustomUserDetails containing the User ID")
    @Test
    void testLoadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(mockUser));

        // Act
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername("admin@test.com");

        // Assert
        assertNotNull(userDetails, "UserDetails should not be null");
        assertEquals(1L, userDetails.getId(), "The custom ID must be extracted successfully");
        assertEquals("admin@test.com", userDetails.getUsername(), "Email should be mapped to username");
        assertEquals("encodedPassword123", userDetails.getPassword(), "Password should be mapped correctly");
        
        // Check if the ADMIN role was converted to a Spring Security authority correctly
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")), 
                   "Authorities must contain the mapped user role");

        verify(userRepository, times(1)).findByEmail("admin@test.com");
    }
    
    /**
     * Validates that searching for a non-existent email results in a 
     * {@link UsernameNotFoundException}, as expected by the Spring Security framework.
     */
    @DisplayName("Load User - Should throw UsernameNotFoundException when email is not in database")
    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("unknown@test.com");
        });

        verify(userRepository, times(1)).findByEmail("unknown@test.com");
    }
}