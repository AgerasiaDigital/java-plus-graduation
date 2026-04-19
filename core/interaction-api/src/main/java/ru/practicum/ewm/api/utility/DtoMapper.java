package ru.practicum.ewm.api.utility;


import org.mapstruct.Mapper;
import ru.practicum.ewm.api.client.user.dto.UserDto;
import ru.practicum.ewm.api.client.user.dto.UserShortDto;

@Mapper(componentModel = "spring")
public interface DtoMapper {

    UserShortDto toUserShortDto(UserDto userDto);

}
