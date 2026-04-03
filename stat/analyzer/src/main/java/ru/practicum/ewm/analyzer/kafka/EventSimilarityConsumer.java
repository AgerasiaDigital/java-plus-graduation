package ru.practicum.ewm.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.analyzer.model.EventSimilarity;
import ru.practicum.ewm.analyzer.model.EventSimilarityId;
import ru.practicum.ewm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {

    private final EventSimilarityRepository eventSimilarityRepository;

    @KafkaListener(
            topics = "stats.events-similarity.v1",
            groupId = "${spring.kafka.consumer.group-id-similarity}",
            containerFactory = "similarityListenerContainerFactory"
    )
    @Transactional
    public void consume(EventSimilarityAvro avro) {
        log.debug("Analyzer received similarity: eventA={}, eventB={}, score={}",
                avro.getEventA(), avro.getEventB(), avro.getScore());

        EventSimilarityId id = new EventSimilarityId(avro.getEventA(), avro.getEventB());
        EventSimilarity entity = eventSimilarityRepository.findById(id)
                .orElse(new EventSimilarity());
        entity.setId(id);
        entity.setScore(avro.getScore());
        entity.setTimestamp(Instant.ofEpochMilli(avro.getTimestamp()));
        eventSimilarityRepository.save(entity);
    }
}
