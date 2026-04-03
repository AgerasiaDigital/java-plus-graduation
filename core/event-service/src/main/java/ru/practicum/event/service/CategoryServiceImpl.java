package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.CategoryDto;
import ru.practicum.event.dto.NewCategoryDto;
import ru.practicum.event.exception.ConflictException;
import ru.practicum.event.exception.NotFoundException;
import ru.practicum.event.mapper.CategoryMapper;
import ru.practicum.event.model.Category;
import ru.practicum.event.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto create(NewCategoryDto request) {
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new ConflictException(String.format("Category with name = %s already exists", request.getName()));
        }
        Category category = categoryRepository.save(CategoryMapper.toNewCategory(request));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public void deleteById(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException(String.format("Category with id = %d not found", categoryId));
        }
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("There are events with this category");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public CategoryDto update(Long categoryId, CategoryDto request) {
        Optional<Category> maybeCategory = categoryRepository.findById(categoryId);
        if (maybeCategory.isEmpty()) {
            throw new NotFoundException(String.format("Category with id = %d not found", categoryId));
        }
        Category category = maybeCategory.get();
        categoryRepository.findByName(request.getName()).ifPresent(found -> {
            if (!found.getId().equals(categoryId)) {
                throw new ConflictException(String.format("Category with name = %s already exists", request.getName()));
            }
        });
        CategoryMapper.updateFields(category, request);
        categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> find(int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findAll(pageable).get()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(CategoryMapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id = %d not found", categoryId)));
    }
}
