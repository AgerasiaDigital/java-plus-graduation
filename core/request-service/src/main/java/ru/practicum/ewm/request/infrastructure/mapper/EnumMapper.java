package ru.practicum.ewm.request.infrastructure.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.api.client.request.enums.RequestStatus;

@Mapper(componentModel = "spring")
public class EnumMapper {

    public RequestStatus toApiRequestStatus(
        ru.practicum.ewm.request.domain.RequestStatus modelStatus) {
        if (modelStatus == null) {
            return null;
        }
        return RequestStatus.valueOf(modelStatus.name());
    }

    public ru.practicum.ewm.request.domain.RequestStatus toModelRequestStatus(RequestStatus apiStatus) {
        if (apiStatus == null) {
            return null;
        }
        return ru.practicum.ewm.request.domain.RequestStatus.valueOf(apiStatus.name());
    }
}