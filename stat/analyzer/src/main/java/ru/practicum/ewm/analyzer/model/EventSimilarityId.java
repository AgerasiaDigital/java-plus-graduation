package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class EventSimilarityId implements Serializable {

    @Column(name = "event_a")
    private long eventA;

    @Column(name = "event_b")
    private long eventB;

    public EventSimilarityId(long eventA, long eventB) {
        this.eventA = eventA;
        this.eventB = eventB;
    }
}
