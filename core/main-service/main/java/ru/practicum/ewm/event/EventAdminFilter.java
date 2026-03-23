package ru.practicum.ewm.event;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventAdminFilter {
    private List<Long> users;
    private List<String> states;
    private List<Long> categories;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    // работает как alias для параметров запросов
    public List<Long> getInitiator() {
        return users;
    }

    public List<Long> getCategory() {
        return categories;
    }
}