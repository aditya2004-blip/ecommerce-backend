package com.ecommerce.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.dto.LoginDto;
import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.AuthService;
import com.ecommerce.utils.JwtUtil;

/**
 * Implementation of the {@link AuthService} providing core identity management logic.
 * This class coordinates user registration, password encryption, cart provisioning, 
 * and secure authentication using Spring Security's AuthenticationManager.
 */
@Service
public class AuthServiceImpl implements AuthService {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);    
    
	private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
	
    /**
     * Constructs the AuthServiceImpl with necessary security and persistence dependencies.
     */
	@Autowired
    public AuthServiceImpl(UserRepository userRepository, CartRepository cartRepository, 
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }
	
	/**
	 * Processes the registration of a new user.
	 * Performs validation, encodes the password, assigns default roles, and 
	 * automatically provisions a shopping cart for customer-type users.
	 * * @param user The user entity to be registered.
	 * @return A {@link UserDto} containing the registered user's profile information.
	 * @throws BadRequestException if the user name is missing.
	 * @throws ResourceAlreadyExistsException if the email is already in the system.
	 */
	@Override
	@Transactional 
	public UserDto registerUser(User user) {
	    logger.info("Initiating registration process for email: {}", user.getEmail());

	    // Validation: Ensure mandatory fields are present
	    if (user.getName() == null || user.getName().trim().isEmpty()) {
	        throw new BadRequestException("Name is required.");
	    }
	    
	    // Integrity Check: Prevent duplicate email registration
	    if (user.getEmail() == null || userRepository.existsByEmail(user.getEmail())) {
	        throw new ResourceAlreadyExistsException("Email is invalid or already in use.");
	    }

	    logger.debug("Encoding password and setting default role.");
	    // Secure password storage using BCrypt (or configured encoder)
	    user.setPassword(passwordEncoder.encode(user.getPassword()));
	    
	    // Default Role Assignment: Fallback to CUSTOMER if no role is specified
	    if (user.getRole() == null) {
	        user.setRole(Role.CUSTOMER);
	    }

	    User savedUser = userRepository.save(user);
	    logger.info("Successfully saved User with ID: {}", savedUser.getId());

	    // Resource Provisioning: Create an empty cart for every new customer
	    if (savedUser.getRole() == Role.CUSTOMER) {
	        Cart cart = new Cart();
	        cart.setUser(savedUser);
	        cartRepository.save(cart);
	        logger.info("Provisioned empty Cart for User ID: {}", savedUser.getId());
	    }

	    // Construct response DTO
	    UserDto userDto = new UserDto();
	    userDto.setId(savedUser.getId());
	    userDto.setName(savedUser.getName());
	    userDto.setEmail(savedUser.getEmail());
	    userDto.setRole(savedUser.getRole());

	    logger.info("Registration complete for: {}", savedUser.getEmail());
	    return userDto;
	}

	/**
	 * Authenticates a user and generates a security token.
	 * Uses the {@link AuthenticationManager} to verify credentials against the database.
	 * * @param loginDto DTO containing login credentials.
	 * @return A JWT token string for authorized access.
	 * @throws ResourceNotFoundException if user credentials are valid but user data is missing.
	 */
	@Override
	public String loginUser(LoginDto loginDto) {
		
		logger.info("Attempting login authentication for email: {}", loginDto.getEmail());
		
		// Triggers the standard Spring Security authentication flow
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
		
		logger.debug("Spring Security authentication passed. Fetching user details from database.");
		
		// Retrieve user details to embed profile info into the JWT
		User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> { 
                	logger.error("Critical State: Authenticated user missing from database: {}", loginDto.getEmail());
                	return new ResourceNotFoundException("User not found with email: " + loginDto.getEmail());
                });
		
		logger.info("Generating JWT token for successfully authenticated user: {}", user.getEmail());
		
		// Return the signed token containing identity and role claims
		return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
	}
}