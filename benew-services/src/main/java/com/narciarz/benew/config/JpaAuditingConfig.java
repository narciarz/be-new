package com.narciarz.benew.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Configuration for JPA Auditing.
 * 
 * <p>Enables automatic population of audit fields like {@code @CreatedDate} 
 * and {@code @LastModifiedDate} in entities.</p>
 * 
 * <p>This configuration provides a custom {@link DateTimeProvider} that returns
 * {@link OffsetDateTime} instead of the default {@link java.time.LocalDateTime}.
 * This is necessary because our entities use OffsetDateTime for audit fields to maintain
 * timezone information.</p>
 * 
 * <p>Entities must be annotated with {@code @EntityListeners(AuditingEntityListener.class)}
 * to use this functionality.</p>
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {
    
    /**
     * Provides current timestamp as OffsetDateTime for JPA Auditing.
     * 
     * <p>Spring Data JPA Auditing does not support OffsetDateTime by default,
     * so we need to provide a custom DateTimeProvider that returns OffsetDateTime
     * instead of LocalDateTime.</p>
     * 
     * @return DateTimeProvider that returns current OffsetDateTime
     */
    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}

