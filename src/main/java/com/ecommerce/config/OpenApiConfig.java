package com.ecommerce.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;


/**
 * Defines the primary OpenAPI bean used by the SpringDoc library to generate 
 * the Swagger documentation.
 * * Configuration Includes:
 * - Metadata: Title, Version, and Developer Contact details.
 * - Servers: Environment-specific URLs (e.g., Localhost).
 * - Security: Global JWT Bearer Authentication requirement.
 * * @return A fully configured OpenAPI object.
 */

@Configuration
public class OpenApiConfig {

	
    @Bean 
    public OpenAPI customOpenAPI() {
    	// Unique identifier for the JWT security scheme
        final String securitySchemeName = "bearerAuth"; 

        return new OpenAPI()
        		// Apply the security requirement globally to all documented endpoints
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                		// Register the 'Bearer Authentication' scheme in the Swagger UI components
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)))
                .info(new Info()
                        .title("E-Commerce Backend API")
                        .version("1.0")
                        .description("RESTful API with JWT Authentication")
                        .contact(new Contact()
                                .name("Aditya Kiran Pati")
                                .email("adityakiranpati@gmail.com")))
                .servers(List.of(
                		// Default development server endpoint
                        new Server().url("http://localhost:8080").description("Development Server")
                ));
    }
}