package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test suite for {@link UserController}.
 * This class verifies the profile management endpoints, ensuring that 
 * retrieval, update, and deletion operations interact correctly with the 
 * {@link UserService} and return sanitized {@link UserDto} objects to the client.
 */
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    /**
     * The controller under test with mocked dependencies injected.
     */
    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    private User mockUser;
    private UserDto mockUserDto;

    /**
     * Prepares the standalone MockMvc context and common test data 
     * before each individual test case.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("Aditya");
        mockUser.setEmail("aditya@test.com");
        
        mockUserDto = new UserDto();
        mockUserDto.setId(1L);
        mockUserDto.setName("Aditya");
        mockUserDto.setEmail("aditya@test.com");
    }

    /**
     * Verifies that the GET endpoint correctly retrieves user profile details 
     * and maps them to a safe DTO.
     */
    @DisplayName("GET /api/users/{id} - Should return 200 OK and mapped UserDto")
    @Test
    void testGetUserById_Success() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(mockUser);
        when(modelMapper.map(any(User.class), eq(UserDto.class))).thenReturn(mockUserDto);

        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Aditya"))
                .andExpect(jsonPath("$.email").value("aditya@test.com"));

        verify(userService, times(1)).getUserById(1L);
    }

    /**
     * Verifies that the PUT endpoint processes profile updates correctly 
     * and returns the updated DTO.
     */
    @DisplayName("PUT /api/users/{id} - Should return 200 OK and mapped UserDto")
    @Test
    void testUpdateUser_Success() throws Exception {
        // Arrange
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(mockUser);
        when(modelMapper.map(any(User.class), eq(UserDto.class))).thenReturn(mockUserDto);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUser)))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Aditya"));

        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    /**
     * Verifies that the DELETE endpoint successfully triggers account removal 
     * and returns a plain-text confirmation.
     */
    @DisplayName("DELETE /api/users/{id} - Should return 200 OK and success message")
    @Test
    void testDeleteUser_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully."));

        verify(userService, times(1)).deleteUser(1L);
    }
}