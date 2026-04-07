package ru.practicum.ewm.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.model.EventSimilarityEntity;
import ru.practicum.ewm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityKafkaConsumer {

    private final EventSimilarityRepository eventSimilarityRepository;

    @KafkaListener(
            topics = "${ewm.kafka.topic.events-similarity}",
            containerFactory = "eventSimilarityListenerFactory"
    )
    public void consume(EventSimilarityAvro similarity) {
        log.info("Analyzer received similarity: eventA={}, eventB={}, score={}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());

        long eventA = Math.min(similarity.getEventA(), similarity.getEventB());
        long eventB = Math.max(similarity.getEventA(), similarity.getEventB());

        Optional<EventSimilarityEntity> existing =
                eventSimilarityRepository.findByEventAAndEventB(eventA, eventB);

        if (existing.isPresent()) {
            EventSimilarityEntity entity = existing.get();
            entity.setScore(similarity.getScore());
            entity.setUpdatedAt(similarity.getTimestamp());
            eventSimilarityRepository.save(entity);
        } else {
            eventSimilarityRepository.save(EventSimilarityEntity.builder()
                    .eventA(eventA)
                    .eventB(eventB)
                    .score(similarity.getScore())
                    .updatedAt(similarity.getTimestamp())
                    .build());
        }
    }
}
