package ru.practicum.ewm.analyzer.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.application.IngestionService;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityConsumer {

    private final IngestionService ingestionService;

    @KafkaListener(
        id = "similarity-listener",
        containerFactory = "similarityContainerFactory",
        topics = "${kafka.topic.events-similarity}"
    )
    public void consumeSimilarity(EventSimilarityAvro similarity) {
        log.info("Received similarity data for events {} and {}", similarity.getEventA(), similarity.getEventB());
        ingestionService.processEventSimilarity(similarity.getEventA(), similarity.getEventB(), similarity.getScore());
    }

}
