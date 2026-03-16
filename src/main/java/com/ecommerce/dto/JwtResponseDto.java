package com.ecommerce.dto;

/**
 * Data Transfer Object for sending the JSON Web Token (JWT) to the client. This
 * DTO is used specifically during the login process to return a secure bearer
 * token upon successful authentication.
 */

public class JwtResponseDto {

	private String token;

	/**
	 * Constructs a new response containing the generated authentication token.
	 * 
	 * @param token The JWT string.
	 */

	public JwtResponseDto(String token) {
		this.token = token;
	}

	/**
	 * @return The bearer token used for authorizing subsequent API requests.
	 */

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
