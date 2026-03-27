package ru.practicum.usersadmin.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserParam;
import ru.practicum.ewm.service.UserService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/admin/users")
public class UsersAdminController {
    private static final Logger log = LoggerFactory.getLogger(UsersAdminController.class);

    private final UserService userService;

    public UsersAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> get(@RequestParam(required = false) List<Long> ids,
                               @RequestParam(required = false, defaultValue = "0") Integer from,
                               @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.debug("GET /admin/users: ids = {}, from = {}, size = {}", ids, from, size);
        return userService.get(new UserParam(ids, from, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid NewUserRequest request) {
        log.debug("POST /admin/users: {}", request);
        return userService.create(request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long userId) {
        log.debug("DELETE /admin/users/{}", userId);
        userService.deleteById(userId);
    }
}

