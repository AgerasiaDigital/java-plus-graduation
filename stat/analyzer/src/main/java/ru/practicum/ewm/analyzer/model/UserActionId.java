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
public class UserActionId implements Serializable {

    @Column(name = "user_id")
    private long userId;

    @Column(name = "event_id")
    private long eventId;

    public UserActionId(long userId, long eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }
}
