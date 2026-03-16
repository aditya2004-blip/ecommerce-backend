package com.ecommerce.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;

/**
 * Unit test for {@link JwtUtil}.
 * This class validates the utility responsible for the generation, parsing, 
 * and validation of JSON Web Tokens (JWT). It ensures that security claims 
 * (Subject, Role, Expiration) are correctly embedded and retrieved from the tokens.
 */
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    private UserDetails mockUserDetails;
    private String validToken;
    private final String TEST_USERNAME = "aditya@test.com";

    /**
     * Initializes the utility class and uses {@link ReflectionTestUtils} to inject 
     * private configuration fields (secret and expiration) that would normally 
     * be provided by Spring's @Value annotation.
     */
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Injecting test-only configuration
        ReflectionTestUtils.setField(jwtUtil, "secret", "ThisIsAVerySecureSecretKeyForTestingJwtWhichIsAtLeast32Bytes!");
        ReflectionTestUtils.setField(jwtUtil, "JWT_EXPIRATION", 1000L * 60 * 60); // 1 hour

        mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn(TEST_USERNAME);

        validToken = jwtUtil.generateToken(TEST_USERNAME, "CUSTOMER");
    }

    /**
     * Verifies that the token generation logic produces a standard 3-part 
     * JWT string (Header.Payload.Signature).
     */
    @DisplayName("Generate Token - Should create a valid non-null JWT string")
    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken("newuser@test.com", "ADMIN");

        assertNotNull(token, "Generated token should not be null");
        assertEquals(3, token.split("\\.").length, "JWT should follow the Header.Payload.Signature format");
    }

    /**
     * Verifies that the 'sub' (subject) claim is correctly extracted as the username.
     */
    @DisplayName("Extract Username - Should decode token and return the exact username")
    @Test
    void testExtractUsername() {
        String extractedUsername = jwtUtil.extractUsername(validToken);

        assertEquals(TEST_USERNAME, extractedUsername, "The extracted subject must match the original username");
    }

    /**
     * Verifies that the 'exp' claim is correctly parsed and represents a future point in time.
     */
    @DisplayName("Extract Expiration - Should decode token and return a future expiration date")
    @Test
    void testExtractExpiration() {
        Date expirationDate = jwtUtil.extractExpiration(validToken);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()), "Expiration date should be strictly in the future");
    }

    /**
     * Validates the "Happy Path" where the token is active and the 
     * user identity matches the provided UserDetails.
     */
    @DisplayName("Validate Token - Should return TRUE if token is valid and usernames match")
    @Test
    void testValidateToken_Success() {
        Boolean isValid = jwtUtil.validateToken(validToken, mockUserDetails);

        assertTrue(isValid, "Token should be validated successfully for the correct user");
    }

    /**
     * Verifies that the utility prevents a valid token from being used by 
     * a different user identity.
     */
    @DisplayName("Validate Token - Should return FALSE if token username does not match UserDetails")
    @Test
    void testValidateToken_WrongUser_ReturnsFalse() {
        UserDetails wrongUser = mock(UserDetails.class);
        when(wrongUser.getUsername()).thenReturn("hacker@test.com");

        Boolean isValid = jwtUtil.validateToken(validToken, wrongUser);

        assertFalse(isValid, "Validation must fail if the token subject does not match the UserDetails principal");
    }

    /**
     * Tests the security boundary for token expiration.
     * By setting a negative expiration time, we force the library to 
     * throw an {@link ExpiredJwtException} during the parsing phase.
     */
    @DisplayName("Validate Token - Should throw ExpiredJwtException if token is expired")
    @Test
    void testValidateToken_ExpiredToken_ThrowsException() {
        // Manipulate the utility to generate tokens already in the past
        ReflectionTestUtils.setField(jwtUtil, "JWT_EXPIRATION", -1000L);
        
        String expiredToken = jwtUtil.generateToken(TEST_USERNAME, "CUSTOMER");

        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.validateToken(expiredToken, mockUserDetails);
        }, "Security logic should immediately reject expired tokens by throwing an exception");
    }
}