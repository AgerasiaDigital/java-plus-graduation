package ru.practicum.ewm.aggregator.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.application.SimilarityCalculationService;
import ru.practicum.ewm.aggregator.domain.UserActionType;
import ru.practicum.ewm.stats.kafka.UserActionAvro;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionConsumer {

    private final SimilarityCalculationService similarityCalculationService;

    @KafkaListener(
        id = "action-listener",
        topics = "${kafka.topic.user-actions}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeAction(UserActionAvro action) {
        log.info("Received action from user {} on event {}", action.getUserId(), action.getEventId());

        UserActionType actionType = UserActionType.valueOf(action.getActionType().name());
        similarityCalculationService.updateSimilarities(action.getUserId(), action.getEventId(), actionType);
    }
}
