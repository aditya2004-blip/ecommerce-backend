package com.ecommerce.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for handling JSON Web Tokens (JWT).
 * Provides methods for generating, parsing, and validating tokens used for 
 * stateless authentication across the e-commerce platform.
 */
@Component
public class JwtUtil {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long JWT_EXPIRATION;

	/**
	 * Generates a cryptographic signing key from the configured secret string.
	 * * @return A {@link Key} object for HS256 signing.
	 */
	private Key getSignKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	/**
	 * Extracts the username (subject) stored within the JWT payload.
	 */
	public String extractUsername(String token) {
		logger.trace("Extracting username from JWT token.");
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Retrieves the expiration date from the JWT payload.
	 */
	public Date extractExpiration(String token) {
		logger.trace("Extracting expiration date from JWT token.");
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Generic method to extract specific claims from the token using a resolver function.
	 * * @param <T> The expected type of the claim.
	 * @param token The raw JWT.
	 * @param claimsResolver Functional interface to resolve the specific claim.
	 * @return The resolved claim value.
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Decodes and parses the JWT payload using the signing key.
	 * This process ensures the token has not been tampered with.
	 */
	private Claims extractAllClaims(String token) {
		logger.trace("Parsing all claims from JWT token.");
		return Jwts.parserBuilder()
				.setSigningKey(getSignKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	/**
	 * Verifies if the token has passed its expiration time.
	 */
	private Boolean isTokenExpired(String token) {
		boolean isExpired = extractExpiration(token).before(new Date());
		if (isExpired) {
			logger.debug("JWT token evaluation: Token is expired.");
		}
		return isExpired;
	}

	/**
	 * Orchestrates the creation of a new JWT with custom claims.
	 * * @param username The subject of the token (user's email).
	 * @param role The user's role to be embedded as a claim.
	 * @return A compact, signed JWT string.
	 */
	public String generateToken(String username, String role) {
		logger.debug("Initiating JWT token generation for user: {}", username);
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		return createToken(claims, username);
	}

	/**
	 * Builds the JWT string with standard and custom claims and signs it with HS256.
	 */
	private String createToken(Map<String, Object> claims, String subject) {
		logger.trace("Building and signing JWT token for subject: {}", subject);

		String token = Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();

		logger.debug("Successfully created and signed JWT token for user: {}", subject);
		return token;
	}

	/**
	 * Validates the token against the current UserDetails.
	 * Checks for both identity matching and temporal validity (expiration).
	 * * @param token The JWT string to validate.
	 * @param userDetails The authenticated user details to compare against.
	 * @return True if the token is valid, false otherwise.
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		logger.debug("Validating JWT token against UserDetails for user: {}", userDetails.getUsername());

		final String username = extractUsername(token);
		boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

		if (isValid) {
			logger.debug("JWT token validation successful for user: {}", username);
		} else {
			logger.warn(
					"Security Event: JWT token validation failed. Username match or expiration check failed for user: {}",
					userDetails.getUsername());
		}

		return isValid;
	}
}