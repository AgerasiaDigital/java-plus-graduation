package ru.practicum.ewm.aggregator.application.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.practicum.ewm.aggregator.domain.UserActionType;

@ConfigurationProperties(prefix = "recommendations")
public record RecommendationProperties(Map<String, Double> actionWeights) {

    public RecommendationProperties {
        if (actionWeights == null) {
            actionWeights = new HashMap<>();
        }
    }

    public Double getActionWeight(UserActionType actionType) {
        return this.actionWeights.get(actionType.name());
    }
}
