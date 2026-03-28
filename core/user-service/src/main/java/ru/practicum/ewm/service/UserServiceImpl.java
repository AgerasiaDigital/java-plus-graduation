package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserParam;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> get(UserParam param) {
        log.debug("Get users request: {}", param);

        int page = param.getFrom() / param.getSize();
        Pageable pageable = PageRequest.of(page, param.getSize());

        Page<User> users;
        if (param.getIds() != null && !param.getIds().isEmpty()) {
            users = userRepository.findByIdIn(param.getIds(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.getContent().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto create(NewUserRequest request) {
        log.debug("New user request: {}", request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("User with email = {} already exists", request.getEmail());
            throw new ConflictException(String.format("User with email = %s already exists", request.getEmail()));
        }

        User user = userRepository.save(UserMapper.toNewUser(request));
        log.info("New user added: {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteById(Long userId) {
        log.debug("Delete user with id = {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} not found", userId);
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        userRepository.deleteById(userId);
        log.info("User with id = {} has been deleted", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.debug("Get user by id: {}", userId);

        Optional<User> maybeUser = userRepository.findById(userId);

        if (maybeUser.isEmpty()) {
            log.warn("User with id = {} not found", userId);
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        return UserMapper.toUserDto(maybeUser.get());
    }
}
