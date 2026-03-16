package com.ecommerce.service;

import com.ecommerce.dto.LoginDto;
import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.User;

/**
 * Service interface defining the contract for authentication and identity management.
 * Handles the business logic for user onboarding (registration) and secure 
 * access control (login/JWT generation).
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     * This process involves validating the user data, encrypting the password, 
     * and persisting the entity to the database.
     * * @param user The user entity containing registration details.
     * @return A {@link UserDto} representing the successfully registered user.
     * @throws com.ecommerce.exception.ResourceAlreadyExistsException if the email is already registered.
     */
    UserDto registerUser(User user);

    /**
     * Authenticates a user based on their credentials.
     * If the credentials are valid, this method generates and returns a 
     * JSON Web Token (JWT) for subsequent authorized requests.
     * * @param loginDto DTO containing the user's email and plain-text password.
     * @return A String containing the signed JWT.
     * @throws org.springframework.security.authentication.BadCredentialsException if authentication fails.
     */
    String loginUser(LoginDto loginDto);
}