package ru.practicum.ewm.event.infrastructure.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.api.client.event.enums.EventState;

@Mapper(componentModel = "spring")
public class EnumMapper {

    public EventState toApiEventState(ru.practicum.ewm.event.domain.EventState eventState) {
        if (eventState == null) {
            return null;
        }
        return EventState.valueOf(eventState.name());
    }

    public ru.practicum.ewm.event.domain.EventState toModelEventState(EventState eventState) {
        if (eventState == null) {
            return null;
        }
        return ru.practicum.ewm.event.domain.EventState.valueOf(eventState.name());
    }
}