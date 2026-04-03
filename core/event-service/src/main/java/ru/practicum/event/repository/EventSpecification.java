package ru.practicum.event.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.event.filter.EventAdminFilter;
import ru.practicum.event.filter.EventInitiatorIdFilter;
import ru.practicum.event.filter.EventPublicFilter;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecification {
    public static Specification<Event> withInitiatorId(EventInitiatorIdFilter f) {
        return Specification.where(initiatorId(f.getInitiator()));
    }

    public static Specification<Event> withPublicFilter(EventPublicFilter f) {
        return Specification
                .where(paid(f.getPaid()))
                .and(text(f.getText()))
                .and(categories(f.getCategory()))
                .and(eventDateAfter(f.getRangeStart()))
                .and(eventDateBefore(f.getRangeEnd()))
                .and(states(f.getEventState()))
                .and(eventDateInFutureIfNoRange(f.getRangeStart(), f.getRangeEnd()));
    }

    public static Specification<Event> withAdminFilter(EventAdminFilter f) {
        return Specification
                .where(initiator(f.getInitiator()))
                .and(states(f.getStates()))
                .and(categories(f.getCategory()))
                .and(eventDateAfter(f.getRangeStart()))
                .and(eventDateBefore(f.getRangeEnd()));
    }

    private static Specification<Event> text(String text) {
        if (text == null || text.isBlank()) return null;
        String pattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    private static Specification<Event> paid(Boolean paid) {
        return (root, query, cb) -> paid == null ? null : cb.equal(root.get("paid"), paid);
    }

    private static Specification<Event> eventDateAfter(LocalDateTime start) {
        return (root, query, cb) -> start == null ? null : cb.greaterThanOrEqualTo(root.get("eventDate"), start);
    }

    private static Specification<Event> eventDateBefore(LocalDateTime end) {
        return (root, query, cb) -> end == null ? null : cb.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    private static Specification<Event> categories(List<Long> catIds) {
        return (root, query, cb) -> (catIds == null || catIds.isEmpty())
                ? null : root.get("category").get("id").in(catIds);
    }

    private static Specification<Event> initiator(List<Long> initiatorIds) {
        return (root, query, cb) -> (initiatorIds == null || initiatorIds.isEmpty())
                ? null : root.get("initiatorId").in(initiatorIds);
    }

    private static Specification<Event> initiatorId(Long initiatorId) {
        return (root, query, cb) -> initiatorId == null ? null : cb.equal(root.get("initiatorId"), initiatorId);
    }

    private static Specification<Event> states(List<String> states) {
        return (root, query, cb) -> (states == null || states.isEmpty())
                ? null : root.get("state").in(states);
    }

    private static Specification<Event> eventDateInFutureIfNoRange(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start != null || end != null) return null;
            return cb.greaterThan(root.get("eventDate"), LocalDateTime.now());
        };
    }
}
