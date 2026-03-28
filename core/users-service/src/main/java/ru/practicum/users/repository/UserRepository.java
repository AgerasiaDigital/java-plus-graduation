package ru.practicum.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByIdIn(Collection<Long> ids);
}
