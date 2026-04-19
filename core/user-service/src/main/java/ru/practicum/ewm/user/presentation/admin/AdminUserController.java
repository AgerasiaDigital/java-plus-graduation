package ru.practicum.ewm.user.presentation.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.user.dto.NewUserRequestDto;
import ru.practicum.ewm.api.client.user.dto.UserDto;
import ru.practicum.ewm.user.application.params.GetListUsersParameters;
import ru.practicum.ewm.user.application.UserService;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequestDto newUserDto) {
        log.info("Admin: Received request to add user: {}", newUserDto);
        UserDto result = userService.createUser(newUserDto);
        log.info("Admin: Adding user: {}", result);
        return result;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Admin: Received request to delete user with Id: {}", userId);
        userService.deleteUser(userId);
        log.info("Admin: Delete user with Id: {}", userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Admin: Received request to get list users with parameters: ids {}, from {}, size {}", ids, from, size);
        GetListUsersParameters parameters = GetListUsersParameters.builder()
                .ids(ids)
                .from(from)
                .size(size)
                .build();
        List<UserDto> result = userService.getUsers(parameters);
        log.info("Admin: Received list users: {}", result);
        return result;
    }

}