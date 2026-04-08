package ru.practicum.ewm.api.client.event.dto;

import static ru.practicum.ewm.api.constants.DateTimeConstants.DATE_TIME_FORMAT_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.api.client.user.dto.UserShortDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto implements EventDtoWithRatingAndRequests {
    Long id;
    String annotation;
    CategoryDto category;
    Long confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    LocalDateTime eventDate;
    UserShortDto initiator;
    Boolean paid;
    String title;
    Double rating;
}