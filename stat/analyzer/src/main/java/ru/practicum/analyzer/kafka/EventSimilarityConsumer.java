package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.AnalyzerService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityConsumer {

    private final AnalyzerService analyzerService;

    @KafkaListener(
            topics = "${kafka.topics.event-similarity}",
            groupId = "${spring.kafka.consumer.group-id}-similarity",
            containerFactory = "eventSimilarityKafkaListenerContainerFactory"
    )
    public void consume(EventSimilarityAvro similarity) {
        log.debug("Analyzer consumed similarity: eventA={}, eventB={}, score={}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());
        analyzerService.processSimilarity(
                similarity.getEventA(),
                similarity.getEventB(),
                similarity.getScore()
        );
    }
}
