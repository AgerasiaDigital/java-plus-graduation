package ru.practicum.ewm.collector.infrastructure.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.UserActionAvro;

@Slf4j
@Component
public class UserActionProducer {

    private final String userActionsTopic;

    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;

    public UserActionProducer(
        @Value("${kafka.topic.user-actions}") String userActionsTopic,
        KafkaTemplate<String, UserActionAvro> kafkaTemplate) {
        this.userActionsTopic = userActionsTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserAction(UserActionAvro userAction) {
        log.info("Sending user action to Kafka topic '{}': {}", userActionsTopic, userAction);

        kafkaTemplate.send(userActionsTopic, userAction)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent user action to offset {}",
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send user action: {}", ex.getMessage());
                }
            });
    }
}
