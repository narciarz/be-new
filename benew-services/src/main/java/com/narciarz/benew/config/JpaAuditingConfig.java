package com.narciarz.benew.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for JPA Auditing.
 * 
 * <p>Enables automatic population of audit fields like {@code @CreatedDate} 
 * and {@code @LastModifiedDate} in entities.</p>
 * 
 * <p>Entities must be annotated with {@code @EntityListeners(AuditingEntityListener.class)}
 * to use this functionality.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

