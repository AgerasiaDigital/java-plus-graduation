package ru.practicum.ewm.event.infrastructure.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.domain.Category;
import ru.practicum.ewm.event.domain.CategoryRepository;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpaCategoryRepository;

    @Override
    public Category save(Category category) {
        return jpaCategoryRepository.save(category);
    }

    @Override
    public void deleteById(Long categoryId) {
        jpaCategoryRepository.deleteById(categoryId);
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return jpaCategoryRepository.findById(categoryId);
    }

    @Override
    public List<Category> findAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return jpaCategoryRepository.findAll(pageable).stream()
            .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaCategoryRepository.existsByNameIgnoreCaseAndTrim(name);
    }
}