package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object specifically designed for the user registration process.
 * * This DTO includes Jakarta Bean Validation constraints to ensure that
 * incoming registration data is structurally valid before it reaches the
 * service layer.
 */

public class UserRegistrationDto {

	/**
	 * The full name of the user. Mandatory field to ensure profile completeness.
	 */

	@NotBlank(message = "Name is required")
	private String name;

	/**
	 * The user's unique email address. Validated for correct email syntax to
	 * prevent invalid contact data.
	 */

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	/**
	 * The plain-text password provided during registration. Enforces a minimum
	 * length of 6 characters for baseline account security.
	 */

	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must be at least 6 characters")
	private String password;

	// Getters and Setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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