package ru.practicum.ewm.comment.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.client.comment.dto.CommentAdminDto;
import ru.practicum.ewm.api.client.comment.dto.CommentDto;
import ru.practicum.ewm.api.client.comment.dto.CommentDtoWithAuthor;
import ru.practicum.ewm.api.client.comment.dto.NewCommentDto;
import ru.practicum.ewm.api.client.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.api.client.event.EventClient;
import ru.practicum.ewm.api.client.event.dto.EventInternalDto;
import ru.practicum.ewm.api.client.event.enums.EventState;
import ru.practicum.ewm.api.client.user.UserClient;
import ru.practicum.ewm.api.client.user.dto.UserDto;
import ru.practicum.ewm.api.client.user.dto.UserShortDto;
import ru.practicum.ewm.api.error.BusinessRuleViolationException;
import ru.practicum.ewm.api.error.EntityNotFoundException;
import ru.practicum.ewm.api.utility.DtoMapper;
import ru.practicum.ewm.comment.application.params.AdminCommentSearchParams;
import ru.practicum.ewm.comment.application.params.PublicCommentParameters;
import ru.practicum.ewm.comment.domain.Comment;
import ru.practicum.ewm.comment.domain.CommentRepository;
import ru.practicum.ewm.comment.infrastructure.mapper.CommentMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final UserClient userClient;
    private final CommentRepository commentRepository;
    private final EventClient eventClient;
    private final CommentMapper commentMapper;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForEvent(Long eventId, PublicCommentParameters parameters) {
        EventInternalDto event = eventClient.getEventById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new EntityNotFoundException("Published event", "Id", eventId);
        }
        if (!event.isCommentsEnabled()) {
            return List.of();
        }

        List<Comment> result = commentRepository.findForEvent(
            eventId,
            parameters.getFrom(),
            parameters.getSize(),
            parameters.getSort()
        );

        return enrichCommentsWithAuthors(result, commentMapper.toDtoList(result));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        userClient.checkUserExists(userId);

        List<Comment> result = commentRepository.findForAuthor(
            userId,
            from,
            size
        );

        return enrichCommentsWithAuthors(result, commentMapper.toDtoList(result));
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        UserDto author = userClient.getUserById(userId);

        EventInternalDto event = eventClient.getEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new BusinessRuleViolationException("Событие еще не опубликовано");
        }

        if (!event.isCommentsEnabled()) {
            throw new BusinessRuleViolationException("Комментарии запрещены");
        }

        Comment comment = commentMapper.toComment(newCommentDto);
        comment.setEventId(eventId);
        comment.setAuthorId(userId);

        Comment savedComment = commentRepository.save(comment);
        return enrichCommentsWithAuthors(List.of(savedComment), List.of(commentMapper.toDto(savedComment))).getFirst();
    }

    @Override
    @Transactional
    public CommentDto updateUserComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Optional<Comment> comment = commentRepository.findById(commentId);

        if (comment.isEmpty()) {
            throw new EntityNotFoundException("Комментарий с id " + commentId + " не найден");
        }

        Comment existedComment = comment.get();

        if (!existedComment.getAuthorId().equals(userId)) {
            throw new EntityNotFoundException("Искомый комментарий с id " + commentId + " пользователя с id " + userId + " не найден");
        }

        if (existedComment.isDeleted()) {
            throw new BusinessRuleViolationException("Редактирование невозможно. Комментарий удален");
        }

        if (existedComment.getCreatedOn().isBefore(LocalDateTime.now().minusHours(6))) {
            throw new BusinessRuleViolationException("Время для редактирования истекло");
        }

        existedComment.setText(updateCommentDto.getText());
        existedComment.setEdited(true);

        Comment updatedComment = commentRepository.save(existedComment);
        return enrichCommentsWithAuthors(List.of(updatedComment), List.of(commentMapper.toDto(updatedComment))).getFirst();
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Comment with id=%d not found", commentId)));
        if (!comment.isDeleted()) {
            comment.setDeleted(true);
            commentRepository.save(comment);
        }
    }

    @Override
    @Transactional
    public void deleteUserComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Comment with id=%d not found", commentId)));
        if (!comment.getAuthorId().equals(userId)) {
            throw new EntityNotFoundException(String.format("Comment with id=%d not found for user with id=%d", commentId, userId));
        }
        if (!comment.isDeleted()) {
            comment.setDeleted(true);
            commentRepository.save(comment);
        }
    }

    @Override
    @Transactional
    public CommentAdminDto restoreCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Comment with id=%d not found", commentId)));
        if (comment.isDeleted()) {
            comment.setDeleted(false);
            comment = commentRepository.save(comment);
        }
        return enrichCommentsWithAuthors(List.of(comment), List.of(commentMapper.toAdminDto(comment))).getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentAdminDto> getAllCommentsAdmin(AdminCommentSearchParams searchParams, int from, int size) {
        log.debug("Admin: Searching all comments with params: {}, from={}, size={}", searchParams, from, size);

        List<Comment> commentList = commentRepository.findAllAdmin(searchParams, from, size);

        log.debug("Admin: Found {} comments for the given criteria.", commentList.size());
        return enrichCommentsWithAuthors(commentList, commentMapper.toAdminDtoList(commentList));
    }

    /**
     * Enriches a list of comment DTOs with the information about the comment authors.
     *
     * @param comments A list of Comment objects that provide author IDs.
     * @param dtos A list of DTOs that extend a base type with setAuthor().
     * @param <T> The specific type of the DTO (e.g., CommentDto, CommentAdminDto).
     * @return The same list of DTOs, now with the author field populated.
     */
    private <T extends CommentDtoWithAuthor> List<T> enrichCommentsWithAuthors(List<Comment> comments, List<T> dtos) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        Set<Long> authorIds = comments.stream()
            .map(Comment::getAuthorId)
            .collect(Collectors.toSet());

        Map<Long, UserShortDto> authorMap = userClient.getUsersByIds(new ArrayList<>(authorIds))
            .stream()
            .collect(Collectors.toMap(UserDto::getId, dtoMapper::toUserShortDto));

        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);
            T dto = dtos.get(i);
            dto.setAuthor(authorMap.getOrDefault(comment.getAuthorId(), createUnknownUserDto()));
        }

        return dtos;
    }

    private UserShortDto createUnknownUserDto() {
        UserShortDto unknownUser = new UserShortDto();
        unknownUser.setId(0L);
        unknownUser.setName("Unknown User");
        return unknownUser;
    }
}
