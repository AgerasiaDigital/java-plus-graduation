package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long id;
    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private int participantLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private boolean requestModeration;
    private String state;
    private String title;
    private Double rating;
}
