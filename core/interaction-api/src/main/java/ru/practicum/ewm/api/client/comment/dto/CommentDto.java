package ru.practicum.ewm.api.client.comment.dto;

import static ru.practicum.ewm.api.constants.DateTimeConstants.DATE_TIME_FORMAT_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import ru.practicum.ewm.api.client.user.dto.UserShortDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto implements CommentDtoWithAuthor {

    Long id;

    String text;

    UserShortDto author;

    Long eventId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    LocalDateTime createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    LocalDateTime updatedOn;

    Boolean isEdited;
}