package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.exception.ConflictException;
import ru.practicum.user.exception.NotFoundException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> get(List<Long> ids, int from, int size) {
        if (ids != null && !ids.isEmpty()) {
            return userRepository.findByIdIn(ids).stream()
                    .map(UserMapper::toUserDto)
                    .toList();
        }
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).get()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto create(NewUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException(String.format("User with email = %s already exists", request.getEmail()));
        }
        User user = userRepository.save(UserMapper.toNewUser(request));
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public UserShortDto getShortById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id = %d not found", userId)));
        return UserMapper.toUserShortDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, UserShortDto> getShortByIds(List<Long> ids) {
        return userRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(User::getId, UserMapper::toUserShortDto));
    }
}
