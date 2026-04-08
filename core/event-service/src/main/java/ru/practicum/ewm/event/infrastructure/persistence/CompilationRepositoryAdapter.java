package ru.practicum.ewm.event.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.domain.Compilation;
import ru.practicum.ewm.event.domain.CompilationRepository;

@Component
@RequiredArgsConstructor
public class CompilationRepositoryAdapter implements CompilationRepository {

    private final JpaCompilationRepository jpaCompilationRepository;

    @Override
    public Compilation save(Compilation compilation) {
        return jpaCompilationRepository.save(compilation);
    }

    @Override
    public void deleteById(Long compilationId) {
        jpaCompilationRepository.deleteById(compilationId);
    }

    @Override
    public Optional<Compilation> findById(Long compilationId) {
        return jpaCompilationRepository.findById(compilationId);
    }

    @Override
    public boolean existsById(Long compilationId) {
        return jpaCompilationRepository.existsById(compilationId);
    }

    @Override
    public List<Compilation> findAll(Optional<Boolean> pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return pinned.map(
                aBoolean -> jpaCompilationRepository.findByPinned(aBoolean, pageable).getContent())
            .orElseGet(() -> jpaCompilationRepository.findAll(pageable).getContent());
    }

    @Override
    public boolean existsByTitle(String title) {
        return jpaCompilationRepository.existsByTitleIgnoreCaseAndTrim(title);
    }
}