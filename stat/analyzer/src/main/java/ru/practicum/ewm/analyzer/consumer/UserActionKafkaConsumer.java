package ru.practicum.ewm.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.model.UserActionEntity;
import ru.practicum.ewm.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionKafkaConsumer {

    private final UserActionRepository userActionRepository;

    @KafkaListener(
            topics = "${ewm.kafka.topic.user-actions}",
            containerFactory = "userActionListenerFactory"
    )
    public void consume(UserActionAvro action) {
        log.info("Analyzer received user action: userId={}, eventId={}, type={}",
                action.getUserId(), action.getEventId(), action.getActionType());

        double weight = toWeight(action.getActionType());
        Instant timestamp = action.getTimestamp();

        Optional<UserActionEntity> existing =
                userActionRepository.findByUserIdAndEventId(action.getUserId(), action.getEventId());

        if (existing.isPresent()) {
            UserActionEntity entity = existing.get();
            if (weight > entity.getWeight()) {
                entity.setWeight(weight);
                entity.setActionType(action.getActionType().name());
                entity.setTimestamp(timestamp);
                userActionRepository.save(entity);
            }
        } else {
            userActionRepository.save(UserActionEntity.builder()
                    .userId(action.getUserId())
                    .eventId(action.getEventId())
                    .actionType(action.getActionType().name())
                    .weight(weight)
                    .timestamp(timestamp)
                    .build());
        }
    }

    private double toWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.7;
            case LIKE -> 1.0;
        };
    }
}
