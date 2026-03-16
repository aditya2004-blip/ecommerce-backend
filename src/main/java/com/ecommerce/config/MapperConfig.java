package com.ecommerce.config;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class for object-to-object mapping.
 * Provides a centralized ModelMapper bean to simplify the conversion between 
 * Database Entities and Data Transfer Objects (DTOs).
 */


@Configuration
public class MapperConfig {

	private static final Logger logger = LoggerFactory.getLogger(MapperConfig.class);

	/**
     * Initializes and configures the ModelMapper bean.
     * * @return A configured ModelMapper instance with null-skipping enabled to 
     * prevent overwriting existing data with null values during partial updates.
     */
	
    @Bean
    public ModelMapper modelMapper() {
        logger.info("System Initialization: Configuring ModelMapper bean. Setting SkipNullEnabled to true.");
        
        ModelMapper mapper = new ModelMapper();
        
     // Configuration: Ensures that null source properties are ignored during the mapping process
        mapper.getConfiguration().setSkipNullEnabled(true);
        
        return mapper;
	}
}
