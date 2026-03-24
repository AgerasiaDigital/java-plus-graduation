package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.event.Location;
import ru.practicum.ewm.model.event.StateAction;

import java.time.LocalDateTime;

@Data
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Valid
    @JsonProperty("location")
    private LocationDto locationDto;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;

    @Size(min = 3, max = 120)
    private String title;

    public boolean hasAnnotation() {
        return annotation != null;
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasLocationDto() {
        return locationDto != null;
    }

    public boolean hasPaid() {
        return paid != null;
    }

    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    public boolean hasRequestModeration() {
        return requestModeration != null;
    }

    public boolean hasStateAction() {
        return stateAction != null;
    }

    public boolean hasTitle() {
        return title != null;
    }

    public boolean hasEventDate() {
        return eventDate != null;
    }

    public void applyTo(Event event, Category category, Location location, EventState eventState) {
        if (hasTitle()) {
            event.setTitle(title);
        }

        if (hasAnnotation()) {
            event.setAnnotation(annotation);
        }

        if (hasDescription()) {
            event.setDescription(description);
        }

        if (hasCategory()) {
            event.setCategory(category);
        }

        if (hasLocationDto()) {
            event.setLocation(location);
        }

        if (hasPaid()) {
            event.setPaid(paid);
        }

        if (hasParticipantLimit()) {
            event.setParticipantLimit(participantLimit);
        }

        if (hasRequestModeration()) {
            event.setRequestModeration(requestModeration);
        }

        if (hasStateAction()) {
            event.setState(eventState);
        }

        if (hasEventDate()) {
            event.setEventDate(eventDate);
        }
    }
}
