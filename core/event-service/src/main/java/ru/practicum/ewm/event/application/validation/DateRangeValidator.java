package ru.practicum.ewm.event.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.ewm.event.application.params.SearchParamsWithDateRange;

public class DateRangeValidator implements
    ConstraintValidator<ValidDateRange, SearchParamsWithDateRange> {
    @Override
    public boolean isValid(SearchParamsWithDateRange params, ConstraintValidatorContext context) {
        if (params.getRangeStart() == null || params.getRangeEnd() == null) {
            return true;
        }
        return !params.getRangeStart().isAfter(params.getRangeEnd());
    }
}