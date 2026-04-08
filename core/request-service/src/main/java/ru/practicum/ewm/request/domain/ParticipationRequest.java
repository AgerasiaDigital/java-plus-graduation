package ru.practicum.ewm.request.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "requests", uniqueConstraints = {
        @UniqueConstraint(name = "unique_requester_event", columnNames = {"requester_id", "event_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id", "created"})
@EntityListeners(AuditingEntityListener.class)
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Дата и время создания запроса
     */
    @CreatedDate
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    /**
     * Пользователь, создавший запрос на участие
     */
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    /**
     * Событие, на которое пользователь хочет попасть
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /**
     * Статус запроса (PENDING, CONFIRMED, REJECTED, CANCELED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status;
}

