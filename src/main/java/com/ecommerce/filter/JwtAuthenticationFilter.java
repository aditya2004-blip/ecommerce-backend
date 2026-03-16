package com.ecommerce.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecommerce.serviceImpl.CustomUserDetailsService;
import com.ecommerce.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security filter that intercepts every incoming HTTP request to validate JWT tokens.
 * This filter ensures that a valid 'Bearer' token in the Authorization header is 
 * processed to establish the user's identity within the Spring Security Context.
 * * Extends {@link OncePerRequestFilter} to guarantee a single execution per request dispatch.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService customUserDetailsService;

	/**
	 * Constructor-based injection for JWT utilities and user retrieval services.
	 */
	@Autowired
	public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
		this.jwtUtil = jwtUtil;
		this.customUserDetailsService = customUserDetailsService;
	}

	/**
	 * Core filtering logic that extracts the JWT, validates it, and sets the 
	 * SecurityContextHolder if the token is legitimate.
	 * * @param request The incoming HttpServletRequest.
	 * @param response The outgoing HttpServletResponse.
	 * @param filterChain The Spring Security filter chain.
	 * @throws ServletException If a servlet-related error occurs.
	 * @throws IOException If an I/O error occurs during processing.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		logger.debug("Security Filter triggered for URI: {}", request.getRequestURI());

		final String authorizationHeader = request.getHeader("Authorization");
		String username = null;
		String jwt = null;

		// Check for the presence of the Authorization header and verify the Bearer prefix
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

			logger.debug("Authorization header containing Bearer token found.");

			// Extract the raw token (length of "Bearer " is 7)
			jwt = authorizationHeader.substring(7);
			try {
				username = jwtUtil.extractUsername(jwt);
				logger.debug("Successfully extracted username from JWT: {}", username);
			} catch (Exception e) {
				logger.warn(
						"Security Event: Failed to extract username from JWT. Token might be invalid, expired, or tampered with. Error: {}",
						e.getMessage());
			}
		} else {
			logger.trace("No Bearer token found in the request header. Proceeding as unauthenticated guest.");
		}

		// Proceed with authentication if a username was found and the SecurityContext is not already populated
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			logger.debug("SecurityContext is currently empty. Fetching UserDetails for: {}", username);

			UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

			// Validate token integrity and expiration against user details
			if (jwtUtil.validateToken(jwt, userDetails)) {

				logger.debug("JWT token validated successfully. Configuring Spring Security context for: {}", username);

				// Create an authentication token with user authorities (roles)
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());
				
				// Build and attach request-specific security details (IP, Session ID, etc.)
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				// Finalize authentication for this request thread
				SecurityContextHolder.getContext().setAuthentication(authToken);

				logger.debug("Successfully injected Authentication token into SecurityContextHolder.");

			} else {
				logger.warn("Security Event: JWT token validation failed for user: {}", username);
			}
		}
		
		// Pass the request/response to the next filter (e.g., AuthorizationFilter)
		logger.trace("Delegating request to the next filter in the Spring Security chain.");
		filterChain.doFilter(request, response);
	}
}