package ru.practicum.ewm.api.client.event.dto;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompilationRequestDto {
    Boolean pinned;
    @Size(min = 1, max = 50, message = "Название подборки должно быть от 1 до 50 символов")
    String title;
    List<Long> events;
}