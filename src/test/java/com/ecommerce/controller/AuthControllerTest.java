package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ecommerce.dto.LoginDto;
import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test suite for {@link AuthController}.
 * This class utilizes MockMvc to perform integration-style testing of the REST API layer 
 * in isolation, ensuring that HTTP status codes, JSON response structures, and 
 * data mapping (DTOs) behave as expected.
 */
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private User mockUser;
    private UserDto mockUserDto; 
    private LoginDto mockLoginDto;

    /**
     * Initializes the MockMvc standalone context and prepares test data before each test execution.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        // Prepare a raw user entity for registration simulation
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aditya");
        mockUser.setEmail("aditya@test.com");
        mockUser.setPassword("password123");
        mockUser.setRole(Role.CUSTOMER);

        // Prepare a safe Data Transfer Object (DTO) for the response
        mockUserDto = new UserDto();
        mockUserDto.setId(1L);
        mockUserDto.setName("Aditya");
        mockUserDto.setEmail("aditya@test.com");
        mockUserDto.setRole(Role.CUSTOMER);

        // Prepare credentials for login simulation
        mockLoginDto = new LoginDto();
        mockLoginDto.setEmail("aditya@test.com");
        mockLoginDto.setPassword("password123");
    }

    /**
     * Tests the user registration endpoint.
     * Verifies that the controller:
     * 1. Returns an HTTP 201 (Created) status.
     * 2. Maps the response to a UserDto (ensuring the password field is excluded).
     * 3. Correctingly serializes and deserializes JSON content.
     */
    @DisplayName("POST /register - Should return 201 Created and the safe UserDto")
    @Test
    void testRegisterUser_Success() throws Exception {
        // Arrange: Mock the service to return our safe DTO
        when(authService.registerUser(any(User.class))).thenReturn(mockUserDto);

        // Act & Assert: Execute POST request and verify the JSON response
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUser)))           
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Aditya"))
                .andExpect(jsonPath("$.email").value("aditya@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Critical: Check security boundary
    }

    /**
     * Tests the login authentication endpoint.
     * Verifies that successful credentials result in an HTTP 200 (OK) status 
     * and the presence of a valid JWT token in the response body.
     */
    @DisplayName("POST /login - Should return 200 OK and a JWT token")
    @Test
    void testLoginUser_Success() throws Exception {
        // Arrange: Mock a fake JWT string
        String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fakePayload.fakeSignature";
        when(authService.loginUser(any(LoginDto.class))).thenReturn(fakeToken);

        // Act & Assert: Execute login and verify token delivery
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockLoginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeToken));
    }
}