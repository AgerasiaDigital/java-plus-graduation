package ru.practicum.ewm.user.presentation.internal;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.user.UserClient;
import ru.practicum.ewm.api.client.user.dto.UserDto;
import ru.practicum.ewm.user.application.UserService;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController implements UserClient {

    private final UserService userService;

    @Override
    @GetMapping("/by-ids")
    public List<UserDto> getUsersByIds(@RequestParam("ids") List<Long> ids) {
        return userService.getUsersByIds(ids);
    }

    @Override
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    @ResponseStatus(HttpStatus.OK)
    public void checkUserExists(@PathVariable Long id) {
        userService.checkUserExists(id);
    }
}