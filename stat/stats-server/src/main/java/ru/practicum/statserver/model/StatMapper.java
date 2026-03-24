package ru.practicum.statserver.model;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EndpointHitDto;

@UtilityClass
public class StatMapper {

    public static StatModel createStatModel(EndpointHitDto endpointHitDto) {
        return new StatModel(
                null,
                endpointHitDto.getApp(),
                endpointHitDto.getUri(),
                endpointHitDto.getIp(),
                endpointHitDto.getTimestamp()
        );
    }
}