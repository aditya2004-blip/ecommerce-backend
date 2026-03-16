package com.ecommerce.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.UserService;

/**
 * Implementation of the {@link UserService} for managing user account lifecycles.
 * This service provides the business logic for profile management, 
 * secure password updates, and account removal, ensuring all sensitive 
 * data modifications are logged and properly encrypted.
 */
@Service
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * Constructs the UserServiceImpl with repository and password encoding dependencies.
	 */
	@Autowired
	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Retrieves a user entity by its primary key.
	 * * @param id The unique identifier of the user.
	 * @return The found User entity.
	 * @throws ResourceNotFoundException if the ID does not correspond to a record in the database.
	 */
	@Override
	public User getUserById(Long id) {
		logger.debug("Fetching user details from database for ID: {}", id);

		return userRepository.findById(id).orElseThrow(() -> {
			logger.error("User retrieval failed. No user exists with ID: {}", id);
			return new ResourceNotFoundException("User not found with id: " + id);
		});
	}

	/**
	 * Performs a partial or full update of a user's profile information.
	 * This method carefully checks for non-null updates to avoid overwriting 
	 * existing data with nulls and ensures new passwords are automatically hashed.
	 * * @param id The ID of the user record to modify.
	 * @param updatedDetails An object containing the new field values.
	 * @return The updated User entity after database persistence.
	 */
	@Override
	public User updateUser(Long id, User updatedDetails) {

		logger.info("Initiating profile update for user ID: {}", id);

		User existingUser = getUserById(id);

		// Update name if a new value is provided
		if (updatedDetails.getName() != null) {
			logger.debug("Updating name for user ID: {}", id);
			existingUser.setName(updatedDetails.getName());
		}

		// Update email if a new value is provided
		if (updatedDetails.getEmail() != null) {
			logger.debug("Updating email for user ID: {}", id);
			existingUser.setEmail(updatedDetails.getEmail());
		}

		// Update role (logged as a warning for auditing purposes)
		if (updatedDetails.getRole() != null) {
			logger.warn("Security Event: Modifying role for user ID: {} to {}", id, updatedDetails.getRole());
			existingUser.setRole(updatedDetails.getRole());
		}

		// If a new password is provided, encode it before saving
		if (updatedDetails.getPassword() != null && !updatedDetails.getPassword().isEmpty()) {
			logger.info("Security Event: User ID {} is updating their password.", id);
			existingUser.setPassword(passwordEncoder.encode(updatedDetails.getPassword()));
		}
		
		User savedUser = userRepository.save(existingUser);
		logger.info("Successfully updated profile for user ID: {}", id);

		return savedUser;
	}

	/**
	 * Removes a user account from the persistence layer.
	 * * @param id The unique identifier of the user to be deleted.
	 */
	@Override
	public void deleteUser(Long id) {
		logger.info("Attempting to delete user account for ID: {}", id);

		User existingUser = getUserById(id);
		userRepository.delete(existingUser);

		logger.info("Successfully deleted user account for ID: {}", id);
	}
}