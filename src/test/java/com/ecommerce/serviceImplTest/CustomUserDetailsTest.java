package com.ecommerce.serviceImplTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.serviceImpl.CustomUserDetails;

/**
 * Unit test for {@link CustomUserDetails}.
 * This class ensures that the wrapper used by Spring Security correctly 
 * delegates calls to the underlying domain {@link User} entity. It validates 
 * that custom fields like the User ID are preserved and that the role 
 * is correctly translated into a GrantedAuthority.
 */
public class CustomUserDetailsTest {

    private User mockUser;
    private CustomUserDetails customUserDetails;

    /**
     * Prepares a mock user and its Security wrapper before each test.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(50L);
        mockUser.setEmail("testuser@store.com");
        mockUser.setPassword("securepass");
        mockUser.setRole(Role.CUSTOMER);

        customUserDetails = new CustomUserDetails(mockUser);
    }

    /**
     * Verifies that the custom {@code getId()} method returns the ID from 
     * the underlying User entity. This is vital for retrieving the user's 
     * primary key in controllers via the {@code @AuthenticationPrincipal} annotation.
     */
    @DisplayName("Verify CustomUserDetails successfully exposes the user ID")
    @Test
    void testGetId() {
        assertEquals(50L, customUserDetails.getId(), "getId() should return the underlying User entity's ID");
    }

    /**
     * Validates the mapping of standard Spring Security {@code UserDetails} methods.
     * * This ensures:
     * 1. The email is treated as the username.
     * 2. Authorities contain the correct Role string.
     * 3. Default account status flags (Expired, Locked, Enabled) remain true.
     */
    @DisplayName("Verify standard UserDetails methods map correctly to the User entity")
    @Test
    void testUserDetailsMapping() {
        assertEquals("testuser@store.com", customUserDetails.getUsername());
        assertEquals("securepass", customUserDetails.getPassword());
        
        // Ensure the CUSTOMER role is correctly converted to a GrantedAuthority
        assertTrue(customUserDetails.getAuthorities().contains(new SimpleGrantedAuthority("CUSTOMER")));
        
        // Assert that default security flags are set to permissive states
        assertTrue(customUserDetails.isAccountNonExpired());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());
        assertTrue(customUserDetails.isEnabled());
    }
}