package com.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the OpenAPI/Swagger configuration.
 * This class ensures that the API documentation beans are correctly initialized 
 * and that the security requirements (JWT Bearer Auth) are properly exposed 
 * to the Swagger UI.
 */
@SpringBootTest
@ActiveProfiles("test")
class OpenApiConfigTest {

    @Autowired
    private ApplicationContext context;

    /**
     * Verifies that the OpenAPI bean is present in the application context 
     * and carries the correct metadata.
     */
    @Test
    void testOpenApiBeanExists() {
        OpenAPI openAPI = context.getBean(OpenAPI.class);
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("E-Commerce Backend API");
    }

    /**
     * Ensures that the JWT Security Scheme is correctly configured.
     * This is critical for allowing developers to test protected endpoints 
     * directly via the Swagger UI by providing a Bearer token.
     */
    @Test
    void testSecuritySchemeConfiguration() {
        OpenAPI openAPI = context.getBean(OpenAPI.class);
        
        // Retrieve the security scheme defined in the OpenApiConfig class
        SecurityScheme bearerAuth = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        
        assertThat(bearerAuth).isNotNull();
        assertThat(bearerAuth.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(bearerAuth.getScheme()).isEqualTo("bearer");
        assertThat(bearerAuth.getBearerFormat()).isEqualTo("JWT");
    }
}