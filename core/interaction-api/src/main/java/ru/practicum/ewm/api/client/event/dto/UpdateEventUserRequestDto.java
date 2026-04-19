package ru.practicum.ewm.api.client.event.dto;

import static ru.practicum.ewm.api.constants.DateTimeConstants.DATE_TIME_FORMAT_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventUserRequestDto {

    @Size(min = 20, max = 2000, message = "Annotation length must be between 20 and 2000 characters")
    String annotation;

    Long category;

    @Size(min = 20, max = 7000, message = "Description length must be between 20 and 7000 characters")
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    @Future(message = "Event date must be in the future")
    LocalDateTime eventDate;

    LocationDto location;

    Boolean paid;

    @PositiveOrZero(message = "Participant limit must be positive or zero")
    Integer participantLimit;

    Boolean requestModeration;

    StateActionUser stateAction;

    @Size(min = 3, max = 120, message = "Title length must be between 3 and 120 characters")
    String title;

    public enum StateActionUser {
        SEND_TO_REVIEW,
        CANCEL_REVIEW
    }
}