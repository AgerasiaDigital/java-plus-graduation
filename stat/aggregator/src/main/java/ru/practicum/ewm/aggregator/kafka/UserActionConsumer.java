package ru.practicum.ewm.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.service.SimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private static final String SIMILARITY_TOPIC = "stats.events-similarity.v1";

    private final SimilarityService similarityService;
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    @KafkaListener(topics = "stats.user-actions.v1", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(UserActionAvro action) {
        log.debug("Aggregator received: userId={}, eventId={}", action.getUserId(), action.getEventId());
        List<EventSimilarityAvro> similarities = similarityService.processUserAction(action);
        for (EventSimilarityAvro similarity : similarities) {
            kafkaTemplate.send(SIMILARITY_TOPIC, similarity);
        }
    }
}
