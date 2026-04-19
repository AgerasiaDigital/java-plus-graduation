package ru.practicum.ewm.request.presentation.internal;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.request.RequestClient;
import ru.practicum.ewm.request.application.RequestService;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class InternalRequestController implements RequestClient {

    private final RequestService requestService;

    @Override
    @GetMapping("/confirmed-counts")
    public Map<Long, Long> getConfirmedRequestCounts(Set<Long> eventIds) {
        return requestService.getConfirmedRequestCounts(eventIds);
    }

    @Override
    @RequestMapping(method = RequestMethod.HEAD, value = "/participation")
    public void checkUserParticipation(@RequestParam("userId") Long userId, @RequestParam("eventId") Long eventId) {
        requestService.checkUserParticipation(userId, eventId);
    }
}
