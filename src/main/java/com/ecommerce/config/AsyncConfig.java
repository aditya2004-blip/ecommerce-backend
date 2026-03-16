package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration class to enable asynchronous behavior in the application.
 * * Enables the use of the @Async annotation, allowing methods (such as email notifications)
 * to run in background threads, improving system responsiveness and performance.
 */

@Configuration
@EnableAsync
public class AsyncConfig {

}