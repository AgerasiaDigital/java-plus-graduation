package ru.practicum.ewm.event.application.params;

import java.time.LocalDateTime;

public interface SearchParamsWithDateRange {
    LocalDateTime getRangeStart();
    LocalDateTime getRangeEnd();
}
