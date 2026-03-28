package ru.practicum.ewm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.dto.user.UserDto;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/admin/users/{userId}")
    UserDto getUserById(@PathVariable("userId") Long userId);
}
