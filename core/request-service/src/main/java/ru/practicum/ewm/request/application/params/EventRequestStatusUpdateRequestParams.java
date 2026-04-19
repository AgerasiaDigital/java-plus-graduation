package ru.practicum.ewm.request.application.params;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.practicum.ewm.request.domain.RequestStatus;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class EventRequestStatusUpdateRequestParams {
    private final Long userId;
    private final Long eventId;
    private final List<Long> requestIds;
    private final RequestStatus status;
}
