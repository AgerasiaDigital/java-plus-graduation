package ru.practicum.events.dto;

import ru.practicum.ewm.model.event.EventState;

/**
 * Internal DTO for getting event-related request rules from {@code requests-service}.
 */
public class EventRequestRulesDto {
    private EventState state;
    private Integer participantLimit;
    private Boolean requestModeration;
    private Long initiatorId;

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }

    public Integer getParticipantLimit() {
        return participantLimit;
    }

    public void setParticipantLimit(Integer participantLimit) {
        this.participantLimit = participantLimit;
    }

    public Boolean getRequestModeration() {
        return requestModeration;
    }

    public void setRequestModeration(Boolean requestModeration) {
        this.requestModeration = requestModeration;
    }

    public Long getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(Long initiatorId) {
        this.initiatorId = initiatorId;
    }
}

