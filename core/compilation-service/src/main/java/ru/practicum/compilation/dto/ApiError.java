package ru.practicum.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    private String errors;
    private String reason;
    private String status;
    private String timestamp;
}
