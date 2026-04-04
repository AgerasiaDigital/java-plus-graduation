package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_similarities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_a", "event_b"}))
@Getter
@Setter
@EqualsAndHashCode(of = {"eventA", "eventB"})
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(name = "score", nullable = false)
    private Double score;
}
