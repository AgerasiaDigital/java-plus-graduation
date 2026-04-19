package ru.practicum.ewm.comment.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import ru.practicum.ewm.comment.application.params.AdminCommentSearchParams;

public interface CommentRepository {

    /**
     * Saves a new comment or updates an existing one.
     *
     * @param comment The comment to save.
     * @return The saved comment.
     */
    Comment save(Comment comment);

    /**
     * Finds a comment by its ID.
     *
     * @param commentId The ID of the comment.
     * @return An {@link Optional} containing the comment if found, or empty otherwise.
     */
    Optional<Comment> findById(Long commentId);

    /**
     * Finds all non-deleted comments for a specific event, with pagination and sorting.
     *
     * @param eventId The ID of the event.
     * @param from    The starting index.
     * @param size    The number of comments to return.
     * @param sort    The sorting parameters.
     * @return A list of comments for the event.
     */
    List<Comment> findForEvent(Long eventId, int from, int size, Sort sort);

    /**
     * Finds all non-deleted comments by a specific author, with pagination.
     *
     * @param authorId The ID of the author.
     * @param from     The starting index.
     * @param size     The number of comments to return.
     * @return A list of comments by the author.
     */
    List<Comment> findForAuthor(Long authorId, int from, int size);

    /**
     * Finds all comments based on a set of admin search criteria, with pagination.
     *
     * @param searchParams The criteria for searching comments.
     * @param from         The starting index.
     * @param size         The number of comments to return.
     * @return A list of comments matching the criteria.
     */
    List<Comment> findAllAdmin(AdminCommentSearchParams searchParams, int from, int size);
}