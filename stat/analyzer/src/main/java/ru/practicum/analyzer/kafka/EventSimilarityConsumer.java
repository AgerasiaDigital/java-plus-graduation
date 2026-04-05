package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.RecommendationsService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {

    private final RecommendationsService recommendationsService;

    @KafkaListener(
            topics = "${ewm.kafka.topic.events-similarity}",
            groupId = "${ewm.kafka.consumer.similarity-group}",
            containerFactory = "similarityKafkaListenerFactory"
    )
    public void consume(EventSimilarityAvro row) {
        try {
            Instant at = row.getTimestamp();
            recommendationsService.mergeEventSimilarity(row.getEventA(), row.getEventB(), row.getScore(), at);
        } catch (Exception e) {
            log.error("Similarity consume failed: {}", e.getMessage(), e);
        }
    }
}
