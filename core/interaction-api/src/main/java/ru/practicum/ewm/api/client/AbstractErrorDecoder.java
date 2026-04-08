package ru.practicum.ewm.api.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import java.util.Map;
import java.util.function.Function;
import ru.practicum.ewm.api.error.ApiError;
import ru.practicum.ewm.api.error.RemoteServiceException;
import ru.practicum.ewm.api.utility.ErrorParser;

public abstract class AbstractErrorDecoder implements ErrorDecoder {
    private Map<Integer, Function<String, Exception>> errorHandlers;

    /**
     * The main template method. It uses a lazily-initialized map of handlers
     * provided by the subclass to decode specific errors.
     */
    @Override
    public final Exception decode(String methodKey, Response response) {
        ApiError errorDto = ErrorParser.parseErrorDto(response).orElse(null);
        String errorMessage = (errorDto != null && errorDto.getMessage() != null)
            ? errorDto.getMessage()
            : "No details provided from the remote service.";

        Function<String, Exception> handler = getHandlers().get(response.status());

        if (handler != null) {
            return handler.apply(errorMessage);
        }

        String detailedErrorMessage = String.format("Received status %d from %s with message: %s",
            response.status(), methodKey, errorMessage);

        return new RemoteServiceException(detailedErrorMessage);
    }

    /**
     * Subclasses MUST implement this method to provide their mapping of
     * HTTP status codes to exception-generating functions.
     *
     * @return A map where the key is an HTTP status code and the value is a
     *         function that takes an error message and returns an Exception.
     */
    protected abstract Map<Integer, Function<String, Exception>> getSpecificErrorHandlers();

    /**
     * Lazily initializes and caches the handlers map from the subclass.
     */
    private Map<Integer, Function<String, Exception>> getHandlers() {
        if (this.errorHandlers == null) {
            this.errorHandlers = getSpecificErrorHandlers();
        }
        return this.errorHandlers;
    }
}