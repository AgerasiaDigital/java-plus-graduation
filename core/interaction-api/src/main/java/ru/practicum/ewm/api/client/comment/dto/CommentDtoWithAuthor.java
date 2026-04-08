package ru.practicum.ewm.api.client.comment.dto;

import ru.practicum.ewm.api.client.user.dto.UserShortDto;

public interface CommentDtoWithAuthor {

    void setAuthor(UserShortDto author);
}
