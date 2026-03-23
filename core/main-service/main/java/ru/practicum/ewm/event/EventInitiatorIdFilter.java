package ru.practicum.ewm.event;

import lombok.Data;

@Data
public class EventInitiatorIdFilter {
    private Long userId;

    public Long getInitiator() {
        return userId;
    }
}
