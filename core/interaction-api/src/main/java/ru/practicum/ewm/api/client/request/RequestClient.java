package ru.practicum.ewm.api.client.request;

import java.util.Map;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "request-service", path = "/internal/requests", configuration = RequestClientConfiguration.class)
public interface RequestClient {

    /**
     * Fetches the number of confirmed requests for a given list of event IDs.
     * @param eventIds The list of event IDs to check.
     * @return A Map where the key is the eventId and the value is the count of confirmed requests.
     */
    @GetMapping("/confirmed-counts")
    Map<Long, Long> getConfirmedRequestCounts(@RequestParam("eventIds") Set<Long> eventIds);

    @RequestMapping(method = RequestMethod.HEAD, value = "/participation")
    void checkUserParticipation(@RequestParam("userId") Long userId, @RequestParam("eventId") Long eventId);
}