package ru.practicum.ewm.event.presentation.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.application.EventService;
import ru.practicum.ewm.stats.client.aop.ActionType;
import ru.practicum.ewm.stats.client.aop.LogUserAction;

@RestController
@RequestMapping("/events/{eventId}/like")
@RequiredArgsConstructor
@Slf4j
public class LikeController {

    private final EventService eventService;

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @LogUserAction(ActionType.LIKE)
    public void likeEvent(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("User id={}: Received request to like event id={}", userId, eventId);
        eventService.addLike(userId, eventId);
    }
}
