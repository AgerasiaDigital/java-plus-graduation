package ru.practicum.event.filter;

import lombok.Data;

@Data
public class EventInitiatorIdFilter {
    private Long userId;

    public Long getInitiator() { return userId; }
}
