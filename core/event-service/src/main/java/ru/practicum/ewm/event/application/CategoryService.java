package ru.practicum.ewm.event.application;

import java.util.List;
import ru.practicum.ewm.api.client.event.dto.CategoryDto;
import ru.practicum.ewm.api.client.event.dto.NewCategoryDto;

public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, NewCategoryDto categoryDto);

    CategoryDto getCategoryById(Long categoryId);

    List<CategoryDto> getAllCategories(int from, int size);
}
