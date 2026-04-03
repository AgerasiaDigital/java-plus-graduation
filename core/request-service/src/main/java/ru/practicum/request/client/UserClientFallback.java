package ru.practicum.request.client;

import org.springframework.stereotype.Component;
import ru.practicum.request.exception.NotFoundException;

@Component
public class UserClientFallback implements UserClient {
    @Override
    public Object getById(Long userId) {
        throw new NotFoundException(String.format("User with id = %d not found (user-service unavailable)", userId));
    }
}
