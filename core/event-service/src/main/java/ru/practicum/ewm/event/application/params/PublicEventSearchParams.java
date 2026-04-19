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

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@ValidDateRange
public class PublicEventSearchParams implements SearchParamsWithDateRange {
    private final String text;
    private final List<Long> categories;
    private final Boolean paid;
    @DateTimeFormat(pattern = DATE_TIME_FORMAT_PATTERN)
    private final LocalDateTime rangeStart;
    @DateTimeFormat(pattern = DATE_TIME_FORMAT_PATTERN)
    private final LocalDateTime rangeEnd;
    @Builder.Default
    private final Boolean onlyAvailable = false;
    private final String sort;
}