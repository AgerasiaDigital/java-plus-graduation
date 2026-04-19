package ru.practicum.ewm.event.application.params;

import static ru.practicum.ewm.api.constants.DateTimeConstants.DATE_TIME_FORMAT_PATTERN;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.event.application.validation.ValidDateRange;
import ru.practicum.ewm.event.domain.EventState;

@Getter
@Builder
@EqualsAndHashCode(of = {"users", "states", "categories", "rangeStart", "rangeEnd"})
@AllArgsConstructor
@ValidDateRange
public class AdminEventSearchParams implements SearchParamsWithDateRange {
    private final List<Long> users;
    private final List<EventState> states;
    private final List<Long> categories;
    @DateTimeFormat(pattern = DATE_TIME_FORMAT_PATTERN)
    private final LocalDateTime rangeStart;
    @DateTimeFormat(pattern = DATE_TIME_FORMAT_PATTERN)
    private final LocalDateTime rangeEnd;
}