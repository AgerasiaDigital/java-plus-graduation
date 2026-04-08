package ru.practicum.ewm.api.constants;

import java.time.format.DateTimeFormatter;

public final class DateTimeConstants {

    private DateTimeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Стандартный шаблон формата даты и времени, используемый во всем приложении.
     * Формат: "yyyy-MM-dd HH:mm:ss"
     */
    public static final String DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Предварительно созданный экземпляр DateTimeFormatter для стандартного формата даты и времени.
     * Может быть использован для парсинга и форматирования объектов LocalDateTime.
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

}