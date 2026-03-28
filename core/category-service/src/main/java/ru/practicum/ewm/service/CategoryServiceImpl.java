package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.EventClient;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventClient eventClient;

    @Override
    public CategoryDto create(NewCategoryDto request) {
        log.debug("New category request: {}", request);
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            log.warn("Category with name = {} already exists", request.getName());
            throw new ConflictException(String.format("Category with name = %s already exists", request.getName()));
        }
        Category category = categoryRepository.save(CategoryMapper.toNewCategory(request));
        log.info("New category added: {}", category);
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public void deleteById(Long categoryId) {
        log.debug("Category delete request with id = {}", categoryId);
        if (!categoryRepository.existsById(categoryId)) {
            log.warn("Category with id = {} not found", categoryId);
            throw new NotFoundException(String.format("Category with id = %d not found", categoryId));
        }
        if (eventClient.existsByCategory(categoryId)) {
            log.warn("There are events with this category");
            throw new ConflictException("There are events with this category");
        }
        categoryRepository.deleteById(categoryId);
        log.info("Category with id = {} has been deleted", categoryId);
    }

    @Override
    public CategoryDto update(Long categoryId, CategoryDto request) {
        log.debug("Category update request with id = {}: {}", categoryId, request);
        Optional<Category> maybeCategory = categoryRepository.findById(categoryId);
        if (maybeCategory.isEmpty()) {
            log.warn("Category with id = {} not found", categoryId);
            throw new NotFoundException(String.format("Category with id = %d not found", categoryId));
        }
        Category category = maybeCategory.get();
        categoryRepository.findByName(request.getName())
                .ifPresent(foundCategory -> {
                    if (!foundCategory.getId().equals(categoryId)) {
                        log.warn("Category with name = {} already exists", request.getName());
                        throw new ConflictException(String.format("Category with name = %s already exists", request.getName()));
                    }
                });
        CategoryMapper.updateFields(category, request);
        categoryRepository.save(category);
        log.info("Category with id = {} has been updated", categoryId);
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> find(int from, int size) {
        log.debug("Get categories request: from = {}, size = {}", from, size);
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findAll(pageable).get()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findById(Long categoryId) {
        log.debug("Get category request with id = {}", categoryId);
        Optional<Category> maybeCategory = categoryRepository.findById(categoryId);
        if (maybeCategory.isEmpty()) {
            log.warn("Category with id = {} not found", categoryId);
            throw new NotFoundException(String.format("Category with id = %d not found", categoryId));
        }
        return CategoryMapper.toCategoryDto(maybeCategory.get());
    }
}
