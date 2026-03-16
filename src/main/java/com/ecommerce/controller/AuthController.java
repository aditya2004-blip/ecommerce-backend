package com.ecommerce.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.JwtResponseDto;
import com.ecommerce.dto.LoginDto;
import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.User;
import com.ecommerce.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller responsible for managing User Authentication and Registration.
 * Provides endpoints for creating new accounts and generating secure JWT tokens 
 * for authorized access.
 */

@RestController
@RequestMapping("/api/users")
@Tag(name = "1. Authentication API", description = "Endpoints for User/Admin registration and JWT login")
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	private final AuthService authService;

	/**
	 * Constructor injection to facilitate the loose coupling of Authentication Services.
	 */
	
	@Autowired
	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	
	/**
	 * Endpoint for user registration.
	 * Processes the User entity from the request body and returns a safe UserDto.
	 * * @param user The incoming User entity containing registration details.
	 * @return A ResponseEntity containing the registered UserDto and a 201 Created status.
	 */
	
	@PostMapping("/register")
	@Operation(summary = "Register a new user", description = "Creates a new user account with roles CUSTOMER or ADMIN.")
	public ResponseEntity<UserDto> register(@RequestBody User user) {
	    logger.info("Received POST request at /api/users/register for email: {}", user.getEmail());
	    
	 // Invoke service layer to handle business logic and mapping to DTO
	    UserDto registeredUser = authService.registerUser(user);

	    logger.info("Returning 201 CREATED response for successfully registered user: {}", registeredUser.getEmail());
	    
	    return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
	}
	
	/**
	 * Endpoint for user authentication.
	 * Validates credentials and generates a stateless JWT for the client.
	 * * @param loginDto Data Transfer Object containing user email and password.
	 * @return A ResponseEntity containing the JWT wrapped in a JwtResponseDto.
	 */
	
	@PostMapping("/login")
	@Operation(summary = "User Login", description = "Authenticates user credentials and returns a JWT Bearer Token.")
	public ResponseEntity<JwtResponseDto> login(@RequestBody LoginDto loginDto) {
        logger.info("Received POST request at /api/users/login for email: {}", loginDto.getEmail());
        
        String token = authService.loginUser(loginDto);
        
        logger.info("Returning 200 OK response with JWT token for authenticated user: {}", loginDto.getEmail());
        return ResponseEntity.ok(new JwtResponseDto(token));
	}
}
