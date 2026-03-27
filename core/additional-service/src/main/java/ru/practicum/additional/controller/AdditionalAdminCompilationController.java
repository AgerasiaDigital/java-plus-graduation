package ru.practicum.additional.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.service.CompilationService;

@RestController
@Validated
@RequestMapping("/admin/compilations")
public class AdditionalAdminCompilationController {
    private static final Logger log = LoggerFactory.getLogger(AdditionalAdminCompilationController.class);

    private final CompilationService compilationService;

    public AdditionalAdminCompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Valid NewCompilationDto request) {
        log.debug("POST /admin/compilations: {}", request);
        return compilationService.create(request);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long compId) {
        log.debug("DELETE /admin/compilations/{}", compId);
        compilationService.deleteById(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable @Positive Long compId,
                                   @RequestBody @Valid UpdateCompilationRequest request) {
        log.debug("PATCH /admin/compilations/{}: {}", compId, request);
        return compilationService.update(compId, request);
    }
}

