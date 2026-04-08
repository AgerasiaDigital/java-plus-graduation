package ru.practicum.ewm.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.practicum.ewm.aggregator.application.config.RecommendationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RecommendationProperties.class)
public class AggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregatorApplication.class, args);
    }
}
