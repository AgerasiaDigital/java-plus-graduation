package ru.practicum.ewm.api.client.event.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LocationDto {

    @PositiveOrZero
    private Float lat;

    @PositiveOrZero
    private Float lon;

}