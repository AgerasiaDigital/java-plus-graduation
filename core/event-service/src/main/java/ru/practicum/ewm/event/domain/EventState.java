package ru.practicum.ewm.event.domain;

/**
 * Состояния жизненного цикла события
 */
public enum EventState {
    /**
     * Ожидает модерации
     */
    PENDING,

    /**
     * Опубликовано
     */
    PUBLISHED,

    /**
     * Отменено
     */
    CANCELED
}

