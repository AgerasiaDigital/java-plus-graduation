package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.AnalyzerService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumer {

    private final AnalyzerService analyzerService;

    @KafkaListener(
            topics = "${kafka.topics.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}-user-actions",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consume(UserActionAvro action) {
        log.debug("Analyzer consumed user action: userId={}, eventId={}, type={}",
                action.getUserId(), action.getEventId(), action.getActionType());
        analyzerService.processUserAction(
                action.getUserId(),
                action.getEventId(),
                action.getActionType().toString(),
                action.getTimestamp().toEpochMilli()
        );
    }
}
