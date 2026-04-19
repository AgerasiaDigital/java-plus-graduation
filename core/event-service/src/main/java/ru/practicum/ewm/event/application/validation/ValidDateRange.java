package ru.practicum.ewm.event.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom validation annotation to ensure that for a given date range,
 * the 'rangeStart' field is not after the 'rangeEnd' field.
 * <p>
 * This annotation must be applied at the TYPE level (on the class itself).
 */
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "The start of the date range must not be after the end of the date range.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}