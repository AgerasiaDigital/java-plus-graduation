package ru.practicum.ewm.api.client.request.enums;

/**
 * Статусы запросов на участие в событии
 */
public enum RequestStatus {
    /**
     * Ожидает подтверждения
     */
    PENDING,

    /**
     * Подтвержден
     */
    CONFIRMED,

    /**
     * Отклонен
     */
    REJECTED,

    /**
     * Отменен
     */
    CANCELED
}

