package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.event.dto.UserShortDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/internal/users/{userId}")
    UserShortDto getById(@PathVariable Long userId);

    @GetMapping("/internal/users")
    Map<Long, UserShortDto> getByIds(@RequestParam List<Long> ids);
}
