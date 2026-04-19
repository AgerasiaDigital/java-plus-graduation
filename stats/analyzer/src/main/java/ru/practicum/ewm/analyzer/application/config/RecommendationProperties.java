package ru.practicum.ewm.analyzer.application.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.practicum.ewm.analyzer.domain.UserActionType;

@ConfigurationProperties(prefix = "recommendations")
@Getter
@Setter
public class RecommendationProperties {
    private Map<String, Double> actionWeights;

    private int maxRecentEventsForPrediction = 10;
    private int maxNeighboursForPrediction = 10;

    public Double getActionWeight(UserActionType actionType) {
        return this.actionWeights.get(actionType.name());
    }
}
