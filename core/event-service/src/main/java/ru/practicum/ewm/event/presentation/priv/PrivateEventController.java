package ru.practicum.ewm.event.presentation.priv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.event.dto.EventFullDto;
import ru.practicum.ewm.api.client.event.dto.EventShortDto;
import ru.practicum.ewm.api.client.event.dto.NewEventDto;
import ru.practicum.ewm.api.client.event.dto.UpdateEventUserRequestDto;
import ru.practicum.ewm.event.application.EventService;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateEventController {

    private final EventService eventService;

    /**
     * Получение событий, добавленных текущим пользователем.<br>
     * В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список.
     *
     * @param userId ID текущего пользователя
     * @param from   количество элементов, которые нужно пропустить для формирования текущего набора
     * @param size   количество элементов в наборе
     * @return Список EventShortDto
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsAddedByCurrentUser(
        @PathVariable Long userId,
        @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
        @RequestParam(name = "size", defaultValue = "10") @Positive int size) {

        log.info("User id={}: Received request to get own events, from={}, size={}", userId, from, size);
        List<EventShortDto> events = eventService.getEventsByOwner(userId, from, size);
        log.info("User id={}: Found {} events. From={}, size={}", userId, events.size(), from, size);
        return events;
    }

    /**
     * Получение полной информации о событии, добавленном текущим пользователем.<br>
     * В случае, если события с заданным id не найдено, возвращает статус код 404.
     *
     * @param userId  ID текущего пользователя
     * @param eventId ID события
     * @return EventFullDto с полной информацией о событии
     */
    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getFullEventInfoByOwner(
        @PathVariable Long userId,
        @PathVariable Long eventId) {

        log.info("User id={}: Received request to get full info for event id={}", userId, eventId);
        EventFullDto eventFullDto = eventService.getEventPrivate(userId, eventId);
        log.info("User id={}: Found full info for event id={}: {}", userId, eventId, eventFullDto.getId());
        return eventFullDto;
    }

    /**
     * Добавление нового события текущим пользователем.<br>
     * Новое событие будет добавлено со статусом PENDING и требует модерации.<br>
     * Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)
     *
     * @param userId      ID текущего пользователя
     * @param newEventDto Объект NewEventDto, содержащий данные для создания нового события
     * @return ResponseEntity с EventFullDto созданного события и статусом HTTP 201 CREATED
     */
    @PostMapping
    public ResponseEntity<EventFullDto> addEventPrivate(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Создание нового события {} зарегистрированным пользователем c id {}", newEventDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addEventPrivate(userId, newEventDto));
    }

    /**
     * Изменение события, добавленного текущим пользователем.<br>
     * Обратите внимание:
     * <ul>
     *     <li>изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)</li>
     *     <li>дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)</li>
     * </ul>
     *
     * @param userId                 ID текущего пользователя
     * @param eventId                ID редактируемого события
     * @param updateEventUserRequestDto Новые данные события
     * @return Обновленное EventFullDto
     */
    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByOwner(
        @PathVariable Long userId,
        @PathVariable Long eventId,
        @Valid @RequestBody UpdateEventUserRequestDto updateEventUserRequestDto) {

        log.info("User id={}: Received request to update event id={} with data: {}",
            userId, eventId, updateEventUserRequestDto);

        EventFullDto updatedEvent = eventService.updateEventByOwner(userId, eventId, updateEventUserRequestDto);

        log.info("User id={}: Event id={} updated successfully. New title: {}",
            userId, eventId, updatedEvent.getTitle());
        return updatedEvent;
    }

}