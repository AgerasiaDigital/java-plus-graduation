package ru.practicum.ewm.aggregator.application.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.practicum.ewm.aggregator.domain.UserActionType;

@ConfigurationProperties(prefix = "recommendations")
public record RecommendationProperties(Map<String, Double> actionWeights, int maxUserEventsForSimilarity) {

    public RecommendationProperties {
        if (actionWeights == null) {
            actionWeights = new HashMap<>();
        }
        if (maxUserEventsForSimilarity <= 0) {
            maxUserEventsForSimilarity = 50;
        }
    }

    public Double getActionWeight(UserActionType actionType) {
        return this.actionWeights.get(actionType.name());
    }
}
