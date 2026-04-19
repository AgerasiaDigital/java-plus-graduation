package ru.practicum.ewm.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"category", "compilations"})
@EqualsAndHashCode(of = {"id", "title", "annotation", "eventDate", "publishedOn"})
@EntityListeners(AuditingEntityListener.class)
public class Event {

    /**
     * Уникальный идентификатор события
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Краткая аннотация события
     */
    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;

    /**
     * Полное описание события
     */
    @Column(name = "description", nullable = false, length = 7000)
    private String description;

    /**
     * Дата и время проведения события
     */
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    /**
     * Дата и время создания события
     */
    @CreatedDate
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    /**
     * Дата и время публикации события
     */
    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    /**
     * Флаг платного участия
     */
    @Column(name = "paid", nullable = false)
    private boolean paid;

    /**
     * Лимит участников события (0 - без ограничений)
     */
    @Column(name = "participant_limit", nullable = false)
    private int participantLimit;

    /**
     * Требуется ли модерация заявок на участие
     */
    @Column(name = "request_moderation", nullable = false)
    private boolean requestModeration;

    /**
     * Заголовок события
     */
    @Column(name = "title", nullable = false, length = 120)
    private String title;

    /**
     * Категория события
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Инициатор события
     */
    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    /**
     * Местоположение события
     */
    @Embedded
    private Location location;

    /**
     * Текущее состояние события
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private EventState state;

    /**
     *  Список подборок, в которых присутствует событие (создано для корректной обратной выборки)
     */
    @ManyToMany(mappedBy = "events")
    @Builder.Default
    private Set<Compilation> compilations = new HashSet<>();

    /**
     * Разрешены ли комментарии
     */
    @Column(name = "comments_enabled", nullable = false)
    private boolean commentsEnabled;

}
