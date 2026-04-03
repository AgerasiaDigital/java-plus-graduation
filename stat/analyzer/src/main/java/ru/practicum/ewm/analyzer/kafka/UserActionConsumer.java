package ru.practicum.ewm.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.analyzer.model.UserAction;
import ru.practicum.ewm.analyzer.model.UserActionId;
import ru.practicum.ewm.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private static final Map<ActionTypeAvro, Double> ACTION_WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 1.0,
            ActionTypeAvro.REGISTER, 5.0,
            ActionTypeAvro.LIKE, 10.0
    );

    private final UserActionRepository userActionRepository;

    @KafkaListener(
            topics = "stats.user-actions.v1",
            groupId = "${spring.kafka.consumer.group-id-actions}",
            containerFactory = "userActionListenerContainerFactory"
    )
    @Transactional
    public void consume(UserActionAvro action) {
        log.debug("Analyzer received user action: userId={}, eventId={}", action.getUserId(), action.getEventId());
        double weight = ACTION_WEIGHTS.getOrDefault(action.getActionType(), 1.0);
        UserActionId id = new UserActionId(action.getUserId(), action.getEventId());
        Instant timestamp = Instant.ofEpochMilli(action.getTimestamp());

        Optional<UserAction> existing = userActionRepository.findById(id);
        if (existing.isPresent()) {
            UserAction ua = existing.get();
            if (weight > ua.getWeight()) {
                ua.setWeight(weight);
                ua.setTimestamp(timestamp);
                userActionRepository.save(ua);
            }
        } else {
            UserAction ua = new UserAction();
            ua.setId(id);
            ua.setWeight(weight);
            ua.setTimestamp(timestamp);
            userActionRepository.save(ua);
        }
    }
}
