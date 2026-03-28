package ru.practicum.ewm.dto.event;

import lombok.Data;

@Data
public class EventAdminFilter {
    private String users;
    private String states;
    private String categories;
    private String rangeStart;
    private String rangeEnd;
}
