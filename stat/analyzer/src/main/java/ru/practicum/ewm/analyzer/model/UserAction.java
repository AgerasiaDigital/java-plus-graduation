package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_actions")
@Getter
@Setter
public class UserAction {

    @EmbeddedId
    private UserActionId id;

    @Column(name = "weight", nullable = false)
    private double weight;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}
