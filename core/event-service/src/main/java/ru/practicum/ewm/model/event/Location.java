package ru.practicum.ewm.model.event;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Location {
    private Float lat;
    private Float lon;
}
