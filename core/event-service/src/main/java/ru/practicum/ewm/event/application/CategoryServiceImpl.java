package ru.practicum.ewm.event.application;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.client.event.dto.CategoryDto;
import ru.practicum.ewm.api.client.event.dto.NewCategoryDto;
import ru.practicum.ewm.api.error.EntityAlreadyExistsException;
import ru.practicum.ewm.api.error.EntityDeletedException;
import ru.practicum.ewm.api.error.EntityNotFoundException;
import ru.practicum.ewm.event.domain.Category;
import ru.practicum.ewm.event.domain.CategoryRepository;
import ru.practicum.ewm.event.domain.EventRepository;
import ru.practicum.ewm.event.infrastructure.mapper.CategoryMapper;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (!categoryRepository.existsByName(newCategoryDto.getName())) {
            return categoryMapper.toDto(categoryRepository
                    .save(categoryMapper.toCategory(newCategoryDto)));
        } else {
            throw new EntityAlreadyExistsException("Category", "name", newCategoryDto.getName());
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category", "Id", categoryId));

        if (categoryRepository.existsByName(newCategoryDto.getName()) &&
                !category.getName().equalsIgnoreCase(newCategoryDto.getName())) {
            throw new EntityAlreadyExistsException("Category", "name", newCategoryDto.getName());
        }

        if (newCategoryDto.getName() != null && !newCategoryDto.getName().isBlank()) {
            category.setName(newCategoryDto.getName());
        }

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {

        if (categoryRepository.findById(categoryId).isEmpty()) {
            throw new EntityNotFoundException("Category", "Id", categoryId);
        }
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new EntityDeletedException("Category", "name", categoryId);
        } else {
            categoryRepository.deleteById(categoryId);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(int from, int size) {
        return categoryRepository.findAll(from, size).stream()
                .map(categoryMapper::toDto)
                .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category", "Id", categoryId));
        return categoryMapper.toDto(category);
    }

}
