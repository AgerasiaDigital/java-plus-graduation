package ru.practicum.client;

public interface CollectorClient {

    void sendUserAction(long userId, long eventId, String actionType);
}
