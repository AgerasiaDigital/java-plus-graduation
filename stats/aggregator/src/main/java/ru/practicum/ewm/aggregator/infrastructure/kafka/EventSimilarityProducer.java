package ru.practicum.ewm.aggregator.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;

@Component
@Slf4j
public class EventSimilarityProducer {

    private final String eventSimilarityTopic;

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    public EventSimilarityProducer(
        @Value("${kafka.topic.events-similarity}") String eventSimilarityTopic,
        KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate) {
        this.eventSimilarityTopic = eventSimilarityTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEventSimilarity(EventSimilarityAvro eventSimilarity) {
        log.info("Sending event similarity data to Kafka topic '{}': {}", eventSimilarityTopic, eventSimilarity);

        kafkaTemplate.send(eventSimilarityTopic, eventSimilarity)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent event similarity data to offset {}",
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send event similarity data: {}", ex.getMessage());
                }
            });
    }
}
