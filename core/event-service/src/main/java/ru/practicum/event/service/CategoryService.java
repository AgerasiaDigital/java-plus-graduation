package ru.practicum.event.service;

import ru.practicum.event.dto.CategoryDto;
import ru.practicum.event.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto request);

    void deleteById(Long categoryId);

    CategoryDto update(Long categoryId, CategoryDto request);

    List<CategoryDto> find(int from, int size);

    CategoryDto findById(Long categoryId);
}
