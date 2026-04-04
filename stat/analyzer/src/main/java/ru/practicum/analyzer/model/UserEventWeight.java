package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_event_weights",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
@Getter
@Setter
@EqualsAndHashCode(of = {"userId", "eventId"})
public class UserEventWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "max_weight", nullable = false)
    private Double maxWeight;

    @Column(name = "last_interaction_ts", nullable = false)
    private Long lastInteractionTs;
}
