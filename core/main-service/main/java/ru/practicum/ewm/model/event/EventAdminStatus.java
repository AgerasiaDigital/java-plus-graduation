package ru.practicum.ewm.model.event;

import java.util.Optional;

public enum EventAdminStatus {
    PUBLISH_EVENT,
    REJECT_EVENT;

    public static Optional<EventAdminStatus> from(String stringStatus) {
        for (EventAdminStatus state : values()) {
            if (state.name().equalsIgnoreCase(stringStatus)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
