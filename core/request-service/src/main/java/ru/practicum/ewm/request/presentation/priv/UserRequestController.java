package ru.practicum.ewm.request.presentation.priv;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.application.RequestService;
import ru.practicum.ewm.stats.client.aop.ActionType;
import ru.practicum.ewm.stats.client.aop.LogUserAction;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserRequestController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @LogUserAction(value = ActionType.REGISTER)
    public ParticipationRequestDto createRequest(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId) {
        log.info("Private: Received request to add user {} in event: {}", userId, eventId);
        ParticipationRequestDto result = requestService.createRequest(userId, eventId);
        log.info("Private: Adding user: {}", result);
        return result;
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        log.info("Private: Received request user {} to cancel request with Id: {}", userId, requestId);
        ParticipationRequestDto result = requestService.cancelRequest(userId, requestId);
        log.info("Private: Cancel request: {}", result);
        return result;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        log.info("Private: Received request to get list participation requests for user {}", userId);
        List<ParticipationRequestDto> result = requestService.getRequests(userId);
        log.info("Private: Received list participation requests for user {}: {}", userId, result);
        return result;
    }

}