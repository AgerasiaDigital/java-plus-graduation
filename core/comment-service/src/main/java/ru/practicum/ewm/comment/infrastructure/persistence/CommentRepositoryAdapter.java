package ru.practicum.ewm.comment.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.comment.application.params.AdminCommentSearchParams;
import ru.practicum.ewm.comment.domain.Comment;
import ru.practicum.ewm.comment.domain.CommentRepository;
import ru.practicum.ewm.comment.domain.QComment;

@Component
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final JpaCommentRepository jpaCommentRepository;

    @Override
    public Comment save(Comment comment) {
        return jpaCommentRepository.saveAndFlush(comment);
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return jpaCommentRepository.findById(commentId);
    }

    @Override
    public List<Comment> findForEvent(Long eventId, int from, int size, Sort sort) {
        Pageable pageable = PageRequest.of(from / size, size, sort);
        return jpaCommentRepository.findByEventIdAndIsDeletedFalse(eventId, pageable).getContent();
    }

    @Override
    public List<Comment> findForAuthor(Long authorId, int from, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");
        Pageable pageable = PageRequest.of(from / size, size, sort);
        return jpaCommentRepository.findByAuthorIdAndIsDeletedFalse(authorId, pageable).getContent();
    }

    @Override
    public List<Comment> findAllAdmin(AdminCommentSearchParams searchParams, int from, int size) {
        QComment qComment = QComment.comment;
        BooleanBuilder predicate = new BooleanBuilder();

        if (searchParams.getUserId() != null) {
            predicate.and(qComment.authorId.eq(searchParams.getUserId()));
        }
        if (searchParams.getEventId() != null) {
            predicate.and(qComment.eventId.eq(searchParams.getEventId()));
        }
        if (searchParams.getIsDeleted() != null) {
            predicate.and(qComment.isDeleted.eq(searchParams.getIsDeleted()));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");
        Pageable pageable = PageRequest.of(from / size, size, sort);

        return jpaCommentRepository.findAll(predicate, pageable).getContent();
    }
}