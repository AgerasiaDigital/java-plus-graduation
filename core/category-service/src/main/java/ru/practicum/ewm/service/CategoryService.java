package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto request);

    void deleteById(Long categoryId);

    CategoryDto update(Long categoryId, CategoryDto request);

    List<CategoryDto> find(int from, int size);

    CategoryDto findById(Long categoryId);
}
