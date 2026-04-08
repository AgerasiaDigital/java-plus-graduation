package ru.practicum.ewm.event.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    void deleteById(Long categoryId);

    Optional<Category> findById(Long categoryId);

    /**
     * Finds all categories with pagination.
     * The implementation should handle default sorting.
     *
     * @param from The starting index.
     * @param size The number of categories to return.
     * @return A list of categories.
     */
    List<Category> findAll(int from, int size);

    /**
     * Checks if a category with the given name already exists.
     * The check should be case-insensitive and trim whitespace.
     *
     * @param name The name of the category to check.
     * @return true if a category with that name exists, false otherwise.
     */
    boolean existsByName(String name);
}