package ru.practicum.ewm.analyzer.application;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.analyzer.application.config.RecommendationProperties;
import ru.practicum.ewm.analyzer.domain.EventSimilarity;
import ru.practicum.ewm.analyzer.domain.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.domain.UserActionType;
import ru.practicum.ewm.analyzer.domain.UserInteraction;
import ru.practicum.ewm.analyzer.domain.UserInteractionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final UserInteractionRepository interactionRepository;
    private final EventSimilarityRepository similarityRepository;

    private final RecommendationProperties properties;

    @Transactional
    public void processUserAction(long userId, long eventId, UserActionType actionType) {
        log.info("Processing user action: userId={}, eventId={}, actionType={}", userId, eventId, actionType);
        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setEventId(eventId);
        interaction.setWeight(properties.getActionWeight(actionType));
        interactionRepository.save(interaction);
        log.info("Saved user interaction for userId {} and eventId {} with weight {}.", userId, eventId, interaction.getWeight());
    }

    @Transactional
    public void processEventSimilarity(long eventA, long eventB, double similarityScore) {
        log.info("Processing event similarity: eventA={}, eventB={}, score={}", eventA, eventB, similarityScore);
        EventSimilarity similarity = new EventSimilarity();
        similarity.setEventA(eventA);
        similarity.setEventB(eventB);
        similarity.setScore(similarityScore);
        Optional<EventSimilarity> oldSimilarityData = similarityRepository.findByEventAAndEventB(eventA, eventB);
        if (oldSimilarityData.isPresent()) {
            log.debug("Updating existing similarity for event pair ({}, {}). Old score: {}, New score: {}",
                eventA, eventB, oldSimilarityData.get().getScore(), similarityScore);
            similarity.setId(oldSimilarityData.get().getId());
        }
        similarityRepository.save(similarity);
        log.info("{} event similarity for event pair ({}, {}) with score {}.",
            oldSimilarityData.isPresent() ? "Updated" : "Saved", eventA, eventB, similarityScore);
    }
}