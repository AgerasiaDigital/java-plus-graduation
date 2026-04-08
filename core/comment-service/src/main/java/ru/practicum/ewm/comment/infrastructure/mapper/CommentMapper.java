package ru.practicum.ewm.comment.infrastructure.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.ewm.api.client.comment.dto.CommentAdminDto;
import ru.practicum.ewm.api.client.comment.dto.CommentDto;
import ru.practicum.ewm.api.client.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.domain.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    /**
     * Маппинг из NewCommentDto в сущность Comment.
     * Поля author и event должны быть установлены в сервисе отдельно.
     * Поля id, createdOn, updatedOn, isEdited, isDeleted будут установлены автоматически/в логике.
     */
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdOn", ignore = true),
        @Mapping(target = "updatedOn", ignore = true),
        @Mapping(target = "authorId", ignore = true),
        @Mapping(target = "eventId", ignore = true),
        @Mapping(target = "isEdited", ignore = true),
        @Mapping(target = "isDeleted", ignore = true)
    })
    Comment toComment(NewCommentDto newCommentDto);


    /**
     * Маппинг из сущности Comment в CommentDto (для публичного/пользовательского API).
     * Поле eventId извлекается из comment.getEvent().getId().
     * Поле isDeleted не включается.
     */
    @Mappings({
        @Mapping(source = "edited", target = "isEdited"),
        @Mapping(target = "author", ignore = true)
    })
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtoList(List<Comment> comments);

    /**
     * Маппинг из сущности Comment в CommentAdminDto (для административного API).
     * Включает поле isDeleted.
     */
    @Mappings({
        @Mapping(source = "edited", target = "isEdited"),
        @Mapping(source = "deleted", target = "isDeleted"),
        @Mapping(target = "author", ignore = true)
    })
    CommentAdminDto toAdminDto(Comment comment);

    List<CommentAdminDto> toAdminDtoList(List<Comment> comments);

}