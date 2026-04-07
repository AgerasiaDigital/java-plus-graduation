package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", fallback = RequestClientFallback.class)
public interface RequestClient {
    @GetMapping("/internal/requests/count")
    Map<Long, Long> getConfirmedCounts(@RequestParam List<Long> eventIds);

    @GetMapping("/internal/requests/participation")
    boolean isUserParticipant(@RequestParam Long userId, @RequestParam Long eventId);
}
