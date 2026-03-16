package com.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecommerce.filter.JwtAuthenticationFilter;
import com.ecommerce.serviceImpl.CustomUserDetailsService;

/**
 * Main security configuration class for the application.
 * Manages authentication, authorization, and stateless session management 
 * to protect API endpoints using JWT.
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	public static final String[] SWAGGER_URL = {
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/swagger-ui.html",
			"/error"
	};
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    
    

    /**
     * Injects authentication components required for the security filter chain.
     */
    
    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        logger.info("System Initialization: SecurityConfig instantiated with JwtAuthenticationFilter and CustomUserDetailsService.");
    }
    
    /**
     * Defines the security filter chain which dictates endpoint permissions and middleware filters.
     * * @param http The HttpSecurity object used to build security constraints.
     * @return A built SecurityFilterChain.
     */
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	logger.info("System Initialization: Configuring Spring Security Filter Chain...");
    	
        http
     // Disabling CSRF as it is not required for stateless REST APIs
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> {
            	logger.debug("Configuring public and protected API endpoints.");
            	auth
            	.requestMatchers(SWAGGER_URL).permitAll()
            	// Permit all users to register, login, and view product lists
                .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll() // Anyone can view products
             // Any other request must be authenticated with a valid JWT
                .anyRequest().authenticated();}
            )
            .sessionManagement(session -> {
            	
            	logger.debug("Setting Session Creation Policy to STATELESS.");
            	
            	// Enforce stateless session management
            	session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);})
         // Insert JWT filter before the standard UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("System Initialization: Spring Security Filter Chain successfully built.");
        return http.build();
    }
    
    /**
     * Configures the BCrypt hashing algorithm for secure password storage.
     */
    
    @Bean
    public PasswordEncoder passwordEncoder() {
    	logger.info("System Initialization: Configuring BCryptPasswordEncoder bean.");
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Exposes the AuthenticationManager to handle standard login procedures.
     */
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    	logger.info("System Initialization: Exposing AuthenticationManager bean.");
        return config.getAuthenticationManager();
    }
}
