package com.ecommerce.dto;

import com.ecommerce.entity.Role;

/**
 * Data Transfer Object for User profile information.
 * This class serves as a safe container for returning user data through the API, 
 * explicitly excluding sensitive fields like passwords to maintain security.
 */

public class UserDto {
	private Long id;
	private String name;
	private String email;
	private Role role;

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}