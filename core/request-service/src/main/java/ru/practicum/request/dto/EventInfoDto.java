package ru.practicum.request.dto;

import lombok.Data;

// DTO для получения информации о событии из event-service
@Data
public class EventInfoDto {
    private Long id;
    private Long initiatorId;
    private String state;
    private Integer participantLimit;
    private Boolean requestModeration;
}
