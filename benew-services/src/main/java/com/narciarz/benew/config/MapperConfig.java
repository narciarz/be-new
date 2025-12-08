package com.narciarz.benew.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for MapStruct mappers.
 * 
 * <p>Ensures that generated MapStruct mapper implementations annotated
 * with @Component are properly detected by Spring's component scanning.</p>
 * 
 * <p>MapStruct generates mapper implementations at compile time with
 * @Component annotation. This configuration explicitly includes the services
 * package in component scanning to ensure these generated beans are registered.</p>
 */
@Configuration
@ComponentScan(basePackages = "com.narciarz.benew.services")
public class MapperConfig {
    // Configuration class to ensure MapStruct mappers are scanned
}
