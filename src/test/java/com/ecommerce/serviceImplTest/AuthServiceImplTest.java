package com.ecommerce.serviceImplTest;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.dto.LoginDto;
import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.serviceImpl.AuthServiceImpl;
import com.ecommerce.utils.JwtUtil;

/**
 * Unit test suite for {@link AuthServiceImpl}.
 * This class validates the core security business logic of the application, 
 * including user registration with automatic cart creation and JWT-based authentication.
 * * It utilizes Mockito to simulate interactions with persistence (Repositories) 
 * and Spring Security components (AuthenticationManager, PasswordEncoder).
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CartRepository cartRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;
    
    /**
     * The service implementation being tested, with all mocked 
     * security and data dependencies injected.
     */
    @InjectMocks
    private AuthServiceImpl authService;
    
    private User mockUser;
    private LoginDto mockLoginDto;
    
    /**
     * Prepares mock data for registration and login scenarios 
     * before each test execution.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aditya");
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("password123");
        mockUser.setRole(Role.CUSTOMER);

        mockLoginDto = new LoginDto();
        mockLoginDto.setEmail("test@test.com");
        mockLoginDto.setPassword("password123");
    }

    /**
     * Verifies that the registration process fails if the email is already in use.
     * This ensures the unique constraint is handled gracefully at the service layer.
     */
    @DisplayName("Register User - Should throw exception when email is already taken")
    @Test
    void testRegisterUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);
        
        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            authService.registerUser(mockUser);
        });
        
        // Ensure no data was saved after the conflict was detected
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Tests a successful registration flow.
     * Verifies that:
     * 1. The password is encrypted.
     * 2. The user is saved to the database.
     * 3. A shopping cart is automatically initialized for the new user.
     * 4. A sanitized UserDto is returned.
     */
    @DisplayName("Register User - Should successfully register a new user, assign CUSTOMER role, and return UserDto")
    @Test
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(mockUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        UserDto resultDto = authService.registerUser(mockUser);

        // Assert
        assertNotNull(resultDto);
        assertEquals("Aditya", resultDto.getName());
        assertEquals(Role.CUSTOMER, resultDto.getRole());
        assertEquals(1L, resultDto.getId());
        
        // Verify orchestration
        verify(userRepository, times(1)).save(any(User.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    /**
     * Verifies that the login process terminates and throws a 
     * BadCredentialsException if Spring Security authentication fails.
     */
    @DisplayName("Login User - Should throw exception on invalid email or password")
    @Test
    void testLoginUser_BadCredentials_ThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.loginUser(mockLoginDto);
        });

        // Ensure no token is generated for invalid credentials
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    /**
     * Verifies that a ResourceNotFoundException is thrown if the user 
     * cannot be retrieved from the database after a theoretical authentication pass.
     */
    @DisplayName("Login User - Should throw exception if user is not found in database after authentication")
    @Test
    void testLoginUser_UserNotFound_ThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(mockLoginDto.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            authService.loginUser(mockLoginDto);
        });
    }

    /**
     * Tests a successful login flow.
     * Verifies that a valid JWT token is generated and returned 
     * upon successful identity verification.
     */
    @DisplayName("Login User - Should successfully authenticate and return a JWT token")
    @Test
    void testLoginUser_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(mockLoginDto.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(mockUser.getEmail(), Role.CUSTOMER.name())).thenReturn("mockJwtToken");

        // Act
        String token = authService.loginUser(mockLoginDto);

        // Assert
        assertNotNull(token);
        assertEquals("mockJwtToken", token);
        
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(mockUser.getEmail(), Role.CUSTOMER.name());
    }
}