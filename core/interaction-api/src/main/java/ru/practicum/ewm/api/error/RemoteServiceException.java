package ru.practicum.ewm.api.error;

/**
 * A generic runtime exception used to wrap unexpected errors
 * that occur during communication with a remote microservice.
 */
public class RemoteServiceException extends RuntimeException {

    public RemoteServiceException(String message) {
        super(message);
    }

    public RemoteServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}