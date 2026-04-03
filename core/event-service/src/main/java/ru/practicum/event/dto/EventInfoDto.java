package ru.practicum.event.dto;

import lombok.Data;

// Минимальный DTO для межсервисной валидации заявок
@Data
public class EventInfoDto {
    private Long id;
    private Long initiatorId;
    private String state;
    private Integer participantLimit;
    private Boolean requestModeration;
}
