package ru.practicum.ewm.request.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@SuppressWarnings("unused")
public class JpaAuditingConfig {
}