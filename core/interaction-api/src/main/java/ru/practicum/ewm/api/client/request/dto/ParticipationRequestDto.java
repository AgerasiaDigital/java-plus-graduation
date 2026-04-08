package ru.practicum.ewm.api.client.request.dto;

import static ru.practicum.ewm.api.constants.DateTimeConstants.DATE_TIME_FORMAT_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.api.client.request.enums.RequestStatus;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {

    Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    LocalDateTime created;

    @JsonProperty("requester")
    Long requesterId;

    @JsonProperty("event")
    Long eventId;

    RequestStatus status;

}
