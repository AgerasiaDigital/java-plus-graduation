package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.service.RequestService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class RequestInternalController {
    private final RequestService requestService;

    @GetMapping("/count")
    public Map<Long, Long> getConfirmedCounts(@RequestParam List<Long> eventIds) {
        log.debug("INTERNAL GET /internal/requests/count?eventIds={}", eventIds);
        return requestService.getConfirmedCounts(eventIds);
    }

    @GetMapping("/participation")
    public boolean isUserParticipant(@RequestParam Long userId, @RequestParam Long eventId) {
        log.debug("INTERNAL GET /internal/requests/participation?userId={}&eventId={}", userId, eventId);
        return requestService.isUserParticipant(userId, eventId);
    }
}
