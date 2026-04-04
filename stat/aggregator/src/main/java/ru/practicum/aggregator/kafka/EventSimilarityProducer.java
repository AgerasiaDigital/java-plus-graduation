package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProducer {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    @Value("${kafka.topics.event-similarity}")
    private String similarityTopic;

    public void send(EventSimilarityAvro similarity) {
        log.debug("Sending similarity to Kafka: eventA={}, eventB={}, score={}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());
        String key = similarity.getEventA() + "-" + similarity.getEventB();
        kafkaTemplate.send(similarityTopic, key, similarity);
    }
}
