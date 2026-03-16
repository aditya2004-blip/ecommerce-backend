package com.ecommerce.serviceImpl;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecommerce.entity.User;

/**
 * Custom implementation of Spring Security's {@link UserDetails} interface.
 * This class acts as a wrapper around the application's {@link User} entity, 
 * allowing Spring Security to perform authentication and authorization checks 
 * using our domain-specific user data.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * Constructs CustomUserDetails by wrapping the domain user entity.
     * * @param user The User entity retrieved from the database.
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Helper method to retrieve the primary key of the user.
     * Useful for logic that requires the User ID beyond standard authentication.
     * * @return Long representing the user's unique ID.
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Maps the user's internal role to a Spring Security GrantedAuthority.
     * In this implementation, we utilize a single role per user (e.g., "CUSTOMER" or "ADMIN").
     * * @return A collection containing the user's authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Maps the Enum role name to a SimpleGrantedAuthority recognized by Spring Security
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));
    }

    /**
     * Returns the encrypted password used for the authentication process.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the identifier used for authentication. 
     * In this system, the user's email serves as the unique username.
     */
    @Override
    public String getUsername() {
        return user.getEmail(); 
    }

    /**
     * Indicates whether the user's account has expired. 
     * @return true (Account expiration logic not implemented).
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * @return true (Account locking logic not implemented).
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     * @return true (Credentials expiration logic not implemented).
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * @return true (User disabling logic not implemented).
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}