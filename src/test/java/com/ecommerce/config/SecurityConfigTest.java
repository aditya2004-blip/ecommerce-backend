package com.ecommerce.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.filter.JwtAuthenticationFilter;
import com.ecommerce.serviceImpl.CustomUserDetailsService;

/**
 * Unit test for the {@link SecurityConfig} class.
 * This class validates that the fundamental security beans—specifically the 
 * Password Encoder and the Authentication Manager—are correctly defined and 
 * initialized within the Spring context.
 */
@ExtendWith(MockitoExtension.class) 
public class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthFilter;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager mockAuthenticationManager;

    /**
     * The security configuration instance under test.
     */
    @InjectMocks
    private SecurityConfig securityConfig;

    /**
     * Verifies that the {@code passwordEncoder()} bean method provides a 
     * BCrypt implementation. This is essential for ensuring that user 
     * passwords are securely hashed before persistence.
     */
    @DisplayName("Password Encoder Bean - Should return a BCryptPasswordEncoder instance")
    @Test
    void testPasswordEncoderBean() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertNotNull(encoder, "The password encoder bean should not be null");
        assertTrue(encoder instanceof BCryptPasswordEncoder, 
            "The encoder must be an instance of BCryptPasswordEncoder for production-grade security");
    }

    /**
     * Verifies that the {@code authenticationManager()} bean method correctly delegates 
     * to Spring Security's {@link AuthenticationConfiguration}. 
     * This manager is the core component that coordinates the login process.
     */
    @DisplayName("Authentication Manager Bean - Should retrieve the manager from configuration")
    @Test
    void testAuthenticationManagerBean() throws Exception {
        // Arrange: Mock the behavior of the authentication configuration
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthenticationManager);

        // Act: Invoke the bean factory method
        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        // Assert: Ensure the returned manager is correct and the call was traced
        assertNotNull(result, "The authentication manager bean should not be null");
        assertEquals(mockAuthenticationManager, result, 
            "Should return the exact mock manager provided by the configuration");
        
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }
}