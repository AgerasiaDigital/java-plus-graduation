package ru.practicum.ewm.comment.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.comment.domain.Comment;

@Repository
public interface JpaCommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor<Comment> {

    Page<Comment> findByEventIdAndIsDeletedFalse(Long eventId, Pageable pageable);

    Page<Comment> findByAuthorIdAndIsDeletedFalse(Long authorId, Pageable pageable);
}