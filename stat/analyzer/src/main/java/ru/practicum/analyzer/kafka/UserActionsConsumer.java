package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.RecommendationsService;
import ru.practicum.analyzer.service.UserActionWeight;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionsConsumer {

    private final RecommendationsService recommendationsService;

    @KafkaListener(
            topics = "${ewm.kafka.topic.user-actions}",
            groupId = "${ewm.kafka.consumer.user-actions-group}",
            containerFactory = "userActionsKafkaListenerFactory"
    )
    public void consume(UserActionAvro action) {
        try {
            double w = UserActionWeight.of(action.getActionType());
            Instant at = action.getTimestamp();
            recommendationsService.mergeUserAction(action.getUserId(), action.getEventId(), w, at);
        } catch (Exception e) {
            log.error("User action consume failed: {}", e.getMessage(), e);
        }
    }
}
