package ru.practicum.ewm.user.application;

import java.util.List;
import ru.practicum.ewm.api.client.user.dto.NewUserRequestDto;
import ru.practicum.ewm.api.client.user.dto.UserDto;
import ru.practicum.ewm.user.application.params.GetListUsersParameters;

public interface UserService {

    UserDto createUser(NewUserRequestDto newUserDto);

    void deleteUser(Long userId);

    List<UserDto> getUsers(GetListUsersParameters parameters);

    List<UserDto> getUsersByIds(List<Long> ids);

    void checkUserExists(Long id);

    UserDto getUser(Long id);

}
