package ru.practicum.ewm.dto.event;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.ewm.event.OffsetBasedPageRequest;

@Data
public class PageRequestDto {
    private Integer from = 0;
    private Integer size = 10;
    private EventSort sort; // EVENT_DATE, VIEWS, null

    public Pageable toPageable() {
        int offset = (from == null) ? 0 : from;
        int limit = (size == null) ? 10 : size;

        Sort sorting;

        if (sort == EventSort.EVENT_DATE) {
            sorting = Sort.by("eventDate").ascending();
        } else {
            // Если сортировка VIEWS или sort отсутствует → сортировка в БД не нужна
            sorting = Sort.unsorted();
        }

        return new OffsetBasedPageRequest(offset, limit, sorting);
    }
}
