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
@Table(name = "user_event_interaction")
@IdClass(UserEventInteraction.Pk.class)
@Getter
@Setter
@NoArgsConstructor
public class UserEventInteraction {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "max_weight", nullable = false)
    private double maxWeight;

    @Column(name = "last_interaction_at", nullable = false)
    private Instant lastInteractionAt;

    @Getter
    @Setter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private Long userId;
        private Long eventId;
    }
}
