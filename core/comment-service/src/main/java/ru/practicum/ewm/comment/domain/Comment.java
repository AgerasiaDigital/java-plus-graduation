package ru.practicum.ewm.comment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    /**
     * Уникальный идентификатор комментария.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Текст комментария.
     */
    @Column(name = "text", nullable = false, length = 2000)
    private String text;

    /**
     * Дата и время создания комментария. Устанавливается автоматически.
     */
    @CreatedDate
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    /**
     * Дата и время последнего обновления комментария. Устанавливается автоматически при изменении.
     */
    @LastModifiedDate
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    /**
     * Автор комментария.
     */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /**
     * Событие, к которому относится комментарий.
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /**
     * Флаг, указывающий, был ли комментарий отредактирован.
     */
    @Column(name = "is_edited", nullable = false)
    @Builder.Default
    private boolean isEdited = false;

    /**
     * Флаг, указывающий, был ли комментарий удален.
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}