package ru.practicum.compilation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.compilation.dto.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient {
    @GetMapping("/internal/events")
    List<EventShortDto> getByIds(@RequestParam List<Long> ids);
}
