package ru.practicum.ewm.aggregator.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.service.EventSimilarityCalculator;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final EventSimilarityCalculator calculator;
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    @Value("${ewm.kafka.topic.events-similarity}")
    private String similarityTopic;

    @KafkaListener(
            topics = "${ewm.kafka.topic.user-actions}",
            containerFactory = "userActionListenerFactory"
    )
    public void consume(UserActionAvro action) {
        log.info("Consumed user action: userId={}, eventId={}, type={}",
                action.getUserId(), action.getEventId(), action.getActionType());

        List<EventSimilarityAvro> similarities = calculator.processUserAction(action);
        for (EventSimilarityAvro sim : similarities) {
            kafkaTemplate.send(similarityTopic, String.valueOf(sim.getEventA()), sim);
        }
    }
}
