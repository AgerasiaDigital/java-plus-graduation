package ru.practicum.ewm.api.client.event.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.api.client.event.enums.EventState;

/**
 * A DTO for service-to-service communication.
 * Contains the essential fields needed by other services (like comment-service and request-service)
 * to perform their business logic without fetching the full public EventFullDto.
 */
@Data
@NoArgsConstructor
public class EventInternalDto {
    private Long id;
    private Long initiatorId;
    private EventState state;
    private Integer participantLimit;
    private boolean requestModeration;
    private boolean commentsEnabled;
}