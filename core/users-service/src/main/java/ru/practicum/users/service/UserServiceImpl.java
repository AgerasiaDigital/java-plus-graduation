package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> get(UserParam userParam) {
        log.debug("Get user request: {}", userParam);

        if (userParam.hasIds()) {
            // TODO: мб может понадобиться сортировка
            return userRepository.findByIdIn(userParam.ids()).stream()
                    .map(UserMapper::toUserDto)
                    .toList();
        } else {
            // TODO: custom pageable impl?
            int page = userParam.from() / userParam.size();
            Pageable pageable = PageRequest.of(page, userParam.size());

            return userRepository.findAll(pageable).get()
                    .map(UserMapper::toUserDto)
                    .toList();
        }
    }

    @Override
    public UserDto create(NewUserRequest request) {
        log.debug("New user request: {}", request);

        // TODO: можно заменить на попытку записи и отлов DataIntegrityViolationException
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
        log.debug("User delete request with id = {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} not found", userId);
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        userRepository.deleteById(userId);
        log.info("User with id = {} has been deleted", userId);
    }
}
