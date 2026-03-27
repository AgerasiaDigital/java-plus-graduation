package ru.practicum.additional.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "requests-service")
public interface RequestsInternalClient {

    @PostMapping("/internal/requests/confirmed-counts")
    Map<Long, Long> countConfirmedRequestsByEventIds(@RequestBody List<Long> eventIds);
}

