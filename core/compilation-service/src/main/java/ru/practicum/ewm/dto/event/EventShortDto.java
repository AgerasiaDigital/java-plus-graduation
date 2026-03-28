package ru.practicum.ewm.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventShortDto {
    private Long id;
    private String annotation;
    private Long category;
    private Integer confirmedRequests;
    private LocalDateTime eventDate;
    private Long initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
