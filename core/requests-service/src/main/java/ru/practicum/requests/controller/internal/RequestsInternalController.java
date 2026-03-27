package ru.practicum.requests.controller.internal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.repository.RequestRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/requests")
public class RequestsInternalController {
    private final RequestRepository requestRepository;

    public RequestsInternalController(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @PostMapping("/confirmed-counts")
    public Map<Long, Long> confirmedCounts(@RequestBody List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> raw = requestRepository.countConfirmedRequestsByEventIds(eventIds);
        return raw.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}

