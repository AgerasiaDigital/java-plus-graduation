package ru.practicum.ewm.dto.event;

import lombok.Data;
import ru.practicum.ewm.model.request.RequestUpdateAction;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private RequestUpdateAction status;
}
