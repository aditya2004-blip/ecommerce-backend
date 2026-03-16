package com.ecommerce.dto;

/**
 * Data Transfer Object representing user credentials for authentication. * This
 * DTO is used to capture the login request body, isolating sensitive
 * authentication fields from the main User entity.
 */

public class LoginDto {
	private String email;
	private String password;

	// Getters and Setters
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
