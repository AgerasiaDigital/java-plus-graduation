package ru.practicum.ewm.dto.event;

import lombok.Data;

@Data
public class PageRequestDto {
    private Integer from = 0;
    private Integer size = 10;

    public org.springframework.data.domain.Pageable toPageable() {
        int page = from / size;
        return org.springframework.data.domain.PageRequest.of(page, size);
    }
}
