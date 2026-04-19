package ru.practicum.ewm.api.client.request;

import java.util.Map;
import java.util.function.Function;
import ru.practicum.ewm.api.client.AbstractErrorDecoder;
import ru.practicum.ewm.api.error.BusinessRuleViolationException;
import ru.practicum.ewm.api.error.EntityNotFoundException;

public class RequestClientErrorDecoder extends AbstractErrorDecoder {

    @Override
    protected Map<Integer, Function<String, Exception>> getSpecificErrorHandlers() {
        return Map.of(
            404, EntityNotFoundException::new,
            409, BusinessRuleViolationException::new
        );
    }
}