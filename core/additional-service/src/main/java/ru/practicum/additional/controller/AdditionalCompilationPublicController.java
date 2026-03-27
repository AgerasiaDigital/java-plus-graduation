package ru.practicum.additional.controller;

import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.CompilationParam;
import ru.practicum.ewm.service.CompilationService;

import java.util.List;

@RestController
@Validated
@RequestMapping("/compilations")
public class AdditionalCompilationPublicController {
    private static final Logger log = LoggerFactory.getLogger(AdditionalCompilationPublicController.class);

    private final CompilationService compilationService;

    public AdditionalCompilationPublicController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                   @RequestParam(required = false, defaultValue = "0") Integer from,
                                                   @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.debug("GET /compilations: pinned = {}, from = {}, size = {}", pinned, from, size);
        CompilationParam param = new CompilationParam(pinned, from, size);
        return compilationService.getCompilations(param);
    }

    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable @Positive Long compId) {
        log.debug("GET /compilations/{}", compId);
        return compilationService.getById(compId);
    }
}

