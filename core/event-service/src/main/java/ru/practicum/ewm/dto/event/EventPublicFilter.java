package ru.practicum.ewm.dto.event;

import lombok.Data;

@Data
public class EventPublicFilter {
    private String text;
    private Long category;
    Boolean paid;
    private String rangeStart;
    private String rangeEnd;
    private Boolean onlyAvailable = false;
    private String sort = "EVENT_DATE";
}
