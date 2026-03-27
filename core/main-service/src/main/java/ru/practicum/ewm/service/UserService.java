package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserParam;

import java.util.List;

public interface UserService {

    List<UserDto> get(UserParam userParam);

    UserDto create(NewUserRequest request);

    void deleteById(Long userId);
}
