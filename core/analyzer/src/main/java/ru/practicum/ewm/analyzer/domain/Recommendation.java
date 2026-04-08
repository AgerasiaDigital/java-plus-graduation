package ru.practicum.ewm.analyzer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Recommendation {
    private long eventId;
    private double score;
}
