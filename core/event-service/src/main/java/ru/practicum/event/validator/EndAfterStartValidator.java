package ru.practicum.event.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.event.annotation.EndAfterStart;
import ru.practicum.event.filter.EventPublicFilter;

public class EndAfterStartValidator implements ConstraintValidator<EndAfterStart, EventPublicFilter> {
    @Override
    public boolean isValid(EventPublicFilter dto, ConstraintValidatorContext context) {
        if (dto.getRangeStart() == null || dto.getRangeEnd() == null) {
            return true;
        }
        return dto.getRangeEnd().isAfter(dto.getRangeStart());
    }
}
