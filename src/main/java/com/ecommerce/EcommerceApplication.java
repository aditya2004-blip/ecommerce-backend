package com.ecommerce;

/**
 * Main entry point for the E-Commerce Backend System.
 * * This class initializes the Spring Boot application context, performs 
 * component scanning, and triggers auto-configuration.
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

@SpringBootApplication
public class EcommerceApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure()
                .directory("./") // Looks in project root
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        // 2. Map .env entries to System Properties
        // This allows ${VARIABLE_NAME} in application.properties to work
        for (DotenvEntry entry : dotenv.entries()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
		SpringApplication.run(EcommerceApplication.class, args);
	}

}
