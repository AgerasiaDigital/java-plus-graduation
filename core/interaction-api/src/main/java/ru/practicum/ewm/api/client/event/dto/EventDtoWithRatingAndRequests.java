package ru.practicum.ewm.api.client.event.dto;

public interface EventDtoWithRatingAndRequests {
    Long getId();
    void setConfirmedRequests(Long count);
    void setRating(Double rating);
}
