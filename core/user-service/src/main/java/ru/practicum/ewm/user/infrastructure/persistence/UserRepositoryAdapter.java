package ru.practicum.ewm.user.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.user.domain.User;
import ru.practicum.ewm.user.domain.UserRepository;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return jpaUserRepository.findById(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaUserRepository.existsById(id);
    }

    @Override
    public void deleteById(Long userId) {
        jpaUserRepository.deleteById(userId);
    }

    @Override
    public List<User> findAll(int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        return jpaUserRepository.findAll(pageable).getContent();
    }

    @Override
    public List<User> findAllByIdIn(List<Long> ids, int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        return jpaUserRepository.findAllByIdIn(ids, pageable).getContent();
    }

    @Override
    public List<User> findByIdIn(List<Long> ids) {
        return jpaUserRepository.findByIdIn(ids);
    }
}