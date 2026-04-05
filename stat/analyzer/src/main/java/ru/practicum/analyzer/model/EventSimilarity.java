package ru.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "event_similarity")
@IdClass(EventSimilarity.Pk.class)
@Getter
@Setter
@NoArgsConstructor
public class EventSimilarity {

    @Id
    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Id
    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(nullable = false)
    private double score;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private Long eventA;
        private Long eventB;
    }
}
