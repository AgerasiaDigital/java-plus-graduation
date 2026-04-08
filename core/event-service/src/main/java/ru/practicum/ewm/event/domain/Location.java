package ru.practicum.ewm.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Location {

    /**
     * Широта географической точки.
     */
    @Column(name = "lat", nullable = false)
    private Float lat;

    /**
     * Долгота географической точки.
     */
    @Column(name = "lon", nullable = false)
    private Float lon;

}

