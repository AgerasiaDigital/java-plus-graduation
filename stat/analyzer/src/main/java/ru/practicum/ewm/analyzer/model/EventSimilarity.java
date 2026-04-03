package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "event_similarity")
@Getter
@Setter
public class EventSimilarity {

    @EmbeddedId
    private EventSimilarityId id;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public long getEventA() {
        return id.getEventA();
    }

    public long getEventB() {
        return id.getEventB();
    }
}
