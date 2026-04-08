package ru.practicum.ewm.event.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.api.client.event.dto.CategoryDto;
import ru.practicum.ewm.api.client.event.dto.NewCategoryDto;
import ru.practicum.ewm.event.domain.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    default Category fromId(Long id) {
        if (id == null) return null;
        Category category = new Category();
        category.setId(id); //  если нужен только id
        return category;
    }

    @Mapping(target = "id", ignore = true)
    Category toCategory(NewCategoryDto newCategoryDto);

}