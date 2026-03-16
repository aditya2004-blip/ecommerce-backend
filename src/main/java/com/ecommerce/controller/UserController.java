package com.ecommerce.controller;

import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing user profiles and administrative account operations.
 * * Security Configuration:
 * - CUSTOMER: Can only retrieve and update their own profile (enforced via SpEL #id == principal.id).
 * - ADMIN: Has global access to view, update, and delete any user record in the system.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	private final UserService userService;
	private final ModelMapper modelMapper;

	/**
	 * Constructor injection for User business logic and DTO mapping utilities.
	 */
	@Autowired
	public UserController(UserService userService, ModelMapper modelMapper) {
		this.userService = userService;
		this.modelMapper = modelMapper;
	}

	/**
	 * Retrieves a specific user's profile details by their ID.
	 * Secured using SpEL: Admins can view any profile, Customers can only view their own.
	 * * @param id Unique identifier of the user to be retrieved.
	 * @return ResponseEntity containing the mapped UserDto and 200 OK status.
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #id == principal.id)")
	public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
		logger.info("Received GET request at /api/users/{} to fetch user profile.", id);

		// Fetch user entity from the service layer
		User user = userService.getUserById(id);

		logger.info("Successfully fetched profile for User ID: {}. Returning 200 OK.", id);
		// Map Entity to DTO to ensure sensitive data is not exposed
		return ResponseEntity.ok(modelMapper.map(user, UserDto.class));
	}

	/**
	 * Updates the profile information for an existing user.
	 * Secured using SpEL: Admins can update any profile, Customers can only update their own.
	 * * @param id Unique identifier of the user to update.
	 * @param userDetails Entity containing the updated user information.
	 * @return ResponseEntity containing the updated UserDto.
	 */
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #id == principal.id)")
	public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
		logger.info("Received PUT request at /api/users/{} to update user profile.", id);

		User updatedUser = userService.updateUser(id, userDetails);

		logger.info("Successfully updated profile for User ID: {}. Returning 200 OK.", id);
		return ResponseEntity.ok(modelMapper.map(updatedUser, UserDto.class));
	}

	/**
	 * Permanently deletes a user account from the system.
	 * Restricted strictly to users with 'ADMIN' authority. Customers cannot delete accounts.
	 * * @param id Unique identifier of the user to be removed.
	 * @return ResponseEntity containing a success message and 200 OK status.
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
		logger.warn("Security Event: Received DELETE request at /api/users/{} to permanently remove user account.", id);

		// Trigger hard deletion through the service layer
		userService.deleteUser(id);

		logger.info("Successfully deleted User ID: {}. Returning 200 OK.", id);
		return ResponseEntity.ok("User deleted successfully.");
	}
}