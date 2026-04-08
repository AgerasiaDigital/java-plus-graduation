package ru.practicum.ewm.request.infrastructure.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.api.client.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.domain.ParticipationRequest;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface RequestMapper {

    ParticipationRequestDto toRequestDto(ParticipationRequest participationRequest);

}
