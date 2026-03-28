package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventFullDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String annotation;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer confirmedRequests;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime eventDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long initiator;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean paid;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer participantLimit;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean requestModeration;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String state;

    private String title;
}
