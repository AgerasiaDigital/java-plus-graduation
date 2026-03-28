package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.user.UserDto;

import java.time.LocalDateTime;

@Data
public class EventFullDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String annotation;

    private CategoryDto category;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer confirmedRequests;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdOn;

    private String description;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime eventDate;

    private UserDto initiator;

    private LocationDto location;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean paid;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer participantLimit;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime publishedOn;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean requestModeration;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String state;

    private String title;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long views;
}
