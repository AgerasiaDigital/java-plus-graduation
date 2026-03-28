package ru.practicum.users.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserShortDto {
    @NotNull
    private Long id;

    @NotBlank
    @Size(min = 2, max = 250)
    private String name;
}
