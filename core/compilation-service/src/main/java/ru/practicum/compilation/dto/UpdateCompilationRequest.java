package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateCompilationRequest {
    private Set<Long> events;
    private Boolean pinned;

    @Pattern(regexp = "^(?!\\s*$).+")
    @Size(min = 1, max = 50)
    private String title;

    public boolean hasEvents() { return events != null; }
    public boolean hasPinned() { return pinned != null; }
    public boolean hasTitle() { return title != null; }
}
