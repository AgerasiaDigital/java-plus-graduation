package ru.practicum.ewm.user.infrastructure.persistence;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.user.domain.User;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Page<User> findAllByIdIn(List<Long> ids, Pageable pageable);

    List<User> findByIdIn(List<Long> ids);
}