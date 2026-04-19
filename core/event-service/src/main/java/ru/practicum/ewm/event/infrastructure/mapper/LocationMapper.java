package ru.practicum.ewm.event.infrastructure.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.api.client.event.dto.LocationDto;
import ru.practicum.ewm.event.domain.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto locationDto);
}