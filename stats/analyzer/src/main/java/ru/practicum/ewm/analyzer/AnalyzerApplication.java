package ru.practicum.ewm.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import ru.practicum.ewm.analyzer.application.config.RecommendationProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(RecommendationProperties.class)
public class AnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApplication.class, args);
    }
}
