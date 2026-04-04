package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.service.SimilarityCalculator;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final SimilarityCalculator calculator;
    private final EventSimilarityProducer producer;

    @KafkaListener(
            topics = "${kafka.topics.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consume(UserActionAvro action) {
        log.debug("Consumed user action: userId={}, eventId={}, type={}",
                action.getUserId(), action.getEventId(), action.getActionType());

        List<EventSimilarityAvro> similarities = calculator.processAction(
                action.getUserId(),
                action.getEventId(),
                action.getActionType().toString(),
                action.getTimestamp()
        );
        similarities.forEach(producer::send);
    }
}
