package ru.practicum.event.util;

import ru.practicum.event.model.EventState;
import ru.practicum.event.model.StateAction;

public class StateTransitionValidator {
    public static EventState changeState(EventState current, StateAction action, boolean isAdmin) {
        switch (action) {
            case SEND_TO_REVIEW:
                if (current == EventState.CANCELED || current == EventState.PENDING)
                    return EventState.PENDING;
                throw new IllegalStateException("Cannot send to review from the state " + current);
            case CANCEL_REVIEW:
                if (current == EventState.PENDING)
                    return EventState.CANCELED;
                throw new IllegalStateException("Cancel is only allowed from PENDING");
            case PUBLISH_EVENT:
                if (!isAdmin) throw new SecurityException("Only the administrator can publish");
                if (current == EventState.PENDING)
                    return EventState.PUBLISHED;
                throw new IllegalStateException("Can only publish PENDING");
            case REJECT_EVENT:
                if (!isAdmin) throw new SecurityException("Only an administrator can decline");
                if (current == EventState.PENDING)
                    return EventState.CANCELED;
                throw new IllegalStateException("Cancel is only allowed from PENDING");
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }
}
