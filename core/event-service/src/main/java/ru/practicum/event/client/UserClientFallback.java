package ru.practicum.event.client;

import org.springframework.stereotype.Component;
import ru.practicum.event.dto.UserShortDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserClientFallback implements UserClient {
    @Override
    public UserShortDto getById(Long userId) {
        UserShortDto dto = new UserShortDto();
        dto.setId(userId);
        dto.setName("Unavailable");
        return dto;
    }

    @Override
    public Map<Long, UserShortDto> getByIds(List<Long> ids) {
        return ids.stream().collect(Collectors.toMap(id -> id, id -> {
            UserShortDto dto = new UserShortDto();
            dto.setId(id);
            dto.setName("Unavailable");
            return dto;
        }));
    }
}
