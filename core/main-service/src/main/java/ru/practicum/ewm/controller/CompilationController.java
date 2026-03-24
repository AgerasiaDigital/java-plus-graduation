package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.CompilationParam;
import ru.practicum.ewm.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated
public class CompilationController {
    private final CompilationService compilationService;

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