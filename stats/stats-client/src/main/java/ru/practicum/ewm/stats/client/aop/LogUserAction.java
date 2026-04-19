package ru.practicum.ewm.stats.client.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to automatically log a user action after a method executes successfully.
 * <p>
 * This annotation should be placed on a controller method that represents a user
 * interaction with an event. The aspect {@link UserActionAspect} will intercept the call,
 * build a {@link ru.practicum.ewm.stats.grpc.UserActionProto}, and send it to the
 * collector service.
 * <p>
 * The method must be in a context of an HTTP request where the 'X-EWM-USER-ID'
 * header is present.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * {@code
 * @GetMapping("/{eventId}")
 * @LogUserAction(value = ActionType.VIEW, eventId = "#eventId")
 * public EventFullDto getEvent(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") long userId) {
 *     // ...
 * }
 *
 * @PostMapping
 * @LogUserAction(value = ActionType.REGISTER, eventId = "#request.eventId")
 * public ParticipationRequestDto createRequest(@RequestBody NewRequestDto request, ...) {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogUserAction {

    ActionType value();
    String eventId() default "#eventId";
}