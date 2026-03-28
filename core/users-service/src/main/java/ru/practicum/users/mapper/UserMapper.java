package ru.practicum.users.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.users.dto.user.NewUserRequest;
import ru.practicum.users.dto.user.UserDto;
import ru.practicum.users.model.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static User toNewUser(NewUserRequest request) {
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return user;
    }

    public static UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());

        return userDto;
    }
}
