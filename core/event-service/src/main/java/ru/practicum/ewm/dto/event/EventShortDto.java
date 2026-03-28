package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
public class EventShortDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String annotation;

    private CategoryDto category;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer confirmedRequests;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime eventDate;

    private UserShortDto initiator;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean paid;

    private String title;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long views;
}
