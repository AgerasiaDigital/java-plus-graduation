package ru.practicum.additional.controller;

import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.service.CategoryService;

import java.util.List;

@RestController
@Validated
@RequestMapping("/categories")
public class AdditionalCategoryPublicController {
    private static final Logger log = LoggerFactory.getLogger(AdditionalCategoryPublicController.class);

    private final CategoryService categoryService;

    public AdditionalCategoryPublicController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryDto> get(@RequestParam(required = false, defaultValue = "0") Integer from,
                                   @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.debug("GET /categories: from = {}, size = {}", from, size);
        return categoryService.find(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getById(@PathVariable @Positive Long catId) {
        log.debug("GET /categories/{}", catId);
        return categoryService.findById(catId);
    }
}

