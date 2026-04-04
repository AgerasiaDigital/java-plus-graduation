package ru.practicum.client;

import java.util.List;
import java.util.Map;

public interface AnalyzerClient {

    List<Long> getRecommendationsForUser(long userId, int maxResults);

    List<Long> getSimilarEvents(long eventId, long userId, int maxResults);

    Map<Long, Double> getInteractionsCount(List<Long> eventIds);
}
