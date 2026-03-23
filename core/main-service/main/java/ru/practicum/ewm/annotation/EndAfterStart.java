package ru.practicum.ewm.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.ewm.validator.EndAfterStartValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndAfterStartValidator.class)
public @interface EndAfterStart {
    String message() default "end must be after start";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}