package ru.practicum.ewm.dto.event;

import lombok.Data;

// TODO: мб стоит вынести в отдельный пакет
@Data
public class ParticipationRequestDto {
    private String created; // TODO: формат 2022-09-06T21:10:05.432

    private Long event;

    private Long id;

    private Long requester;

    private String status;
}
