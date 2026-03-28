package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserShortDto getById(@PathVariable Long userId) {
        log.debug("INTERNAL GET /internal/users/{}", userId);
        return userService.getShortById(userId);
    }

    @GetMapping
    public Map<Long, UserShortDto> getByIds(@RequestParam List<Long> ids) {
        log.debug("INTERNAL GET /internal/users?ids={}", ids);
        return userService.getShortByIds(ids);
    }
}
