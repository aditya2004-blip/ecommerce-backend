package com.ecommerce.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * This service is responsible for bridging the gap between Spring Security 
 * and our database by retrieving the user's authentication and authorization 
 * data based on their email address.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
	
	private final UserRepository userRepository;

    /**
     * Constructs the service with the necessary User repository.
     * * @param userRepository The repository used to perform database lookups.
     */
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

	/**
	 * Locates the user based on the email provided during the login or JWT validation process.
	 * * @param email The identifier for the user (in this system, the email).
	 * @return A {@link UserDetails} object containing security information.
	 * @throws UsernameNotFoundException if the email does not exist in the database.
	 */
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		logger.debug("Spring Security is attempting to load user details for email: {}", email);
		
		// Attempt to fetch the user from the database
		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			logger.warn("Authentication failure: User not found with email: {}", email);
			return new UsernameNotFoundException("User not found with email: " + email);
		});
		
		logger.debug("Successfully loaded user details and authorities for email: {}. Role: {}", 
                user.getEmail(), user.getRole().name());
		
		// Wrap the domain user entity in our CustomUserDetails for Spring Security consumption
		return new CustomUserDetails(user);
	}
}