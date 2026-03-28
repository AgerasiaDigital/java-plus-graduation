package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<UserDto> get(List<Long> ids, int from, int size);

    UserDto create(NewUserRequest request);

    void deleteById(Long userId);

    UserShortDto getShortById(Long userId);

    Map<Long, UserShortDto> getShortByIds(List<Long> ids);
}
