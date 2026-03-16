package com.ecommerce.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecommerce.serviceImpl.CustomUserDetailsService;
import com.ecommerce.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Unit test suite for {@link JwtAuthenticationFilter}.
 * This class validates the core security middleware responsible for intercepting 
 * HTTP requests, extracting JWTs, and populating the Spring Security Context.
 * * It ensures the filter correctly handles valid tokens, missing headers, 
 * malformed prefixes, and existing authentication states.
 */
@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    /**
     * The filter instance under test with mocked security dependencies.
     */
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Clears the SecurityContext before each test to ensure a clean state 
     * and prevent cross-test contamination.
     */
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Clears the SecurityContext after each test to maintain security integrity.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Verifies that a valid JWT results in the user being successfully 
     * authenticated within the Spring Security Context.
     */
    @DisplayName("Filter - Should set authentication in context when valid token is provided")
    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "aditya@test.com";

        // Arrange: Mock the Authorization header and validation flow
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        // Act: Execute the filter
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert: Check if the principal is stored in the SecurityContext
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be set in the context");
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        // Ensure the filter chain continues processing
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Verifies that the filter ignores requests without an Authorization header 
     * and proceeds without attempting authentication.
     */
    @DisplayName("Filter - Should continue without authenticating if Authorization header is missing")
    @Test
    void testDoFilterInternal_NoHeader_SkipsAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Context should remain empty");
        
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    /**
     * Verifies that non-Bearer tokens (e.g., Basic Auth) are ignored by this specific filter.
     */
    @DisplayName("Filter - Should continue without authenticating if Authorization header doesn't start with Bearer")
    @Test
    void testDoFilterInternal_InvalidHeader_SkipsAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic YWRtaW46cGFzc3dvcmQ=");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    /**
     * Verifies that if the token fails validation (e.g., expired or tampered), 
     * no authentication is set in the context.
     */
    @DisplayName("Filter - Should not set authentication if the token is invalid")
    @Test
    void testDoFilterInternal_InvalidToken_SkipsAuthentication() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        String username = "hacker@test.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        
        // Fail the validation
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false); 

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication must not be set for an invalid token");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Optimization Test: Verifies that if the SecurityContext is already populated, 
     * the filter skips unnecessary database lookups.
     */
    @DisplayName("Filter - Should skip database lookup if user is already authenticated in context")
    @Test
    void testDoFilterInternal_AlreadyAuthenticated_SkipsValidation() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "aditya@test.com";

        // Pre-populate the context
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("existingUser", null, null)
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Ensure the database service was never called
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}