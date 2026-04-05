package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.service.SimilarityAggregatorService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionsListener {

    private final SimilarityAggregatorService similarityAggregatorService;

    @KafkaListener(
            topics = "${ewm.kafka.topic.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(UserActionAvro action) {
        try {
            similarityAggregatorService.onUserAction(action);
        } catch (Exception e) {
            log.error("Failed to process user action: {}", e.getMessage(), e);
        }
    }
}
