package ru.practicum.ewm.event.presentation.admin;

import static ru.practicum.ewm.api.constants.DateTimeConstants.DATE_TIME_FORMAT_PATTERN;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.event.dto.EventFullDto;
import ru.practicum.ewm.api.client.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.ewm.event.application.params.AdminEventSearchParams;
import ru.practicum.ewm.event.application.EventService;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminEventController {

    private final EventService eventService;
    private static final String DATETIME_FORMAT = DATE_TIME_FORMAT_PATTERN;

    /**
     * Поиск событий администратором.
     * Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия.
     * В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список.
     *
     * @param params Объект с параметрами поиска, включающий списки ID пользователей, состояний, категорий,
     *               а также даты начала и окончания диапазона событий
     * @param from       количество событий, которые нужно пропустить для формирования текущего набора
     * @param size       количество событий в наборе
     * @return Список EventFullDto
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> searchEventsAdmin(
        @Valid AdminEventSearchParams params,
        @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
        @RequestParam(name = "size", defaultValue = "10") @Positive int size) {

        log.info(
            "Admin: Received request to search events with params: users={}, states={}, "
                + "categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
            params.getUsers(), params.getStates(), params.getCategories(), params.getRangeStart(),
            params.getRangeEnd(), from, size);

        List<EventFullDto> foundEvents = eventService.getEventsAdmin(
            params,
            from,
            size
        );
        log.info("Admin: Found {} events for the given criteria.", foundEvents.size());
        return foundEvents;
    }

    /**
     * Редактирование данных события и его статуса (отклонение/публикация) администратором.<br>
     * Валидация данных не требуется (согласно старому ТЗ, но DTO содержит аннотации валидации).<br>
     * Обратите внимание:
     * <ul>
     *     <li>дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)</li>
     *     <li>событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)</li>
     *     <li>событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)</li>
     * </ul>
     *
     * @param eventId                   ID события
     * @param updateEventAdminRequestDto Данные для изменения информации о событии
     * @return Обновленное EventFullDto
     */
    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto moderateEventByAdmin(
        @PathVariable Long eventId,
        @Valid @RequestBody UpdateEventAdminRequestDto updateEventAdminRequestDto) {

        log.info("Admin: Received request to moderate event id={} with data: {}",
            eventId, updateEventAdminRequestDto);

        EventFullDto moderatedEvent = eventService.moderateEventByAdmin(eventId, updateEventAdminRequestDto);

        log.info("Admin: Event id={} moderated successfully. New state: {}",
            eventId, moderatedEvent.getState());
        return moderatedEvent;
    }
}