package ru.practicum.client.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "ru.practicum.client.grpc")
public class RecommendationClientsAutoConfiguration {
}
