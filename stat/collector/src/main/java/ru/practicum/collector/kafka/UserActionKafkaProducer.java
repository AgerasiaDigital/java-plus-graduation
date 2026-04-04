package ru.practicum.collector.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionKafkaProducer {

    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;

    @Value("${kafka.topics.user-actions}")
    private String userActionsTopic;

    public void send(UserActionAvro action) {
        log.debug("Sending user action to Kafka: userId={}, eventId={}, type={}",
                action.getUserId(), action.getEventId(), action.getActionType());
        kafkaTemplate.send(userActionsTopic, String.valueOf(action.getUserId()), action);
    }
}
