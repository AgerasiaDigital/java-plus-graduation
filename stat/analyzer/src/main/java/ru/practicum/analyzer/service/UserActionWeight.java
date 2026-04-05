package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

public final class UserActionWeight {

    private UserActionWeight() {
    }

    public static double of(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 1.0;
            case REGISTER -> 2.0;
            case LIKE -> 3.0;
        };
    }
}
