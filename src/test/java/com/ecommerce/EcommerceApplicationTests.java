package com.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Standard Spring Boot test class that ensures the application context 
 * loads correctly without any configuration issues.
 * * * This serves as a smoke test for the entire dependency injection container, 
 * verifying that all beans, repositories, and services (including Redis and Database 
 * configurations) are properly wired.
 */
@SpringBootTest
@ActiveProfiles("test")
class EcommerceApplicationTests {

    /**
     * Verifies that the Spring application context can be started successfully.
     * If there are any circular dependencies or missing mandatory properties 
     * in application.properties, this test will fail.
     */
	@Test
	void contextLoads() {
        // This method remains empty as its primary purpose is to verify 
        // that the application context starts up without throwing exceptions.
	}

}