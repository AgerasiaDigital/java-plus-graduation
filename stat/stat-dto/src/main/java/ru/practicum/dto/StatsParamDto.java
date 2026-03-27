package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsParamDto {
    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;
    private Boolean isUnique;
}
