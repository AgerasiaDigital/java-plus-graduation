package ru.practicum.ewm.request.domain;

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

