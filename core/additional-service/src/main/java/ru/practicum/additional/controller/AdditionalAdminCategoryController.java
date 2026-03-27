package ru.practicum.additional.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.service.CategoryService;

@RestController
@Validated
@RequestMapping("/admin/categories")
public class AdditionalAdminCategoryController {
    private static final Logger log = LoggerFactory.getLogger(AdditionalAdminCategoryController.class);

    private final CategoryService categoryService;

    public AdditionalAdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid NewCategoryDto request) {
        log.debug("POST /admin/categories: {}", request);
        return categoryService.create(request);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long catId) {
        log.debug("DELETE /admin/categories/{}", catId);
        categoryService.deleteById(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto patch(@PathVariable @Positive Long catId,
                               @RequestBody @Valid CategoryDto request) {
        log.debug("PATCH /admin/categories/{}: {}", catId, request);
        return categoryService.update(catId, request);
    }
}

