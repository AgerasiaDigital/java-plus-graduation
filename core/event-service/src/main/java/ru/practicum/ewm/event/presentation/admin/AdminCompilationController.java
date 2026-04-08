package ru.practicum.ewm.event.presentation.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.event.dto.CompilationDto;
import ru.practicum.ewm.api.client.event.dto.NewCompilationDto;
import ru.practicum.ewm.api.client.event.dto.UpdateCompilationRequestDto;
import ru.practicum.ewm.event.application.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Admin: Received request to create compilation: {}", newCompilationDto);
        CompilationDto result = compilationService.saveCompilation(newCompilationDto);
        log.info("Admin: Created compilation: {}", result);
        return result;
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(
            @PathVariable @Positive Long compId,
            @Valid @RequestBody UpdateCompilationRequestDto updateCompilationRequestDto) {
        log.info("Admin: Received request to update compilation id={} with data: {}", compId, updateCompilationRequestDto);
        CompilationDto result = compilationService.updateCompilation(compId, updateCompilationRequestDto);
        log.info("Admin: Updated compilation: {}", result);
        return result;
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable @Positive Long compId) {
        log.info("Admin: Received request to delete compilation with id={}", compId);
        compilationService.deleteCompilation(compId);
        log.info("Admin: Deleted compilation with id={}", compId);
    }
}