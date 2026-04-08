package ru.practicum.ewm.api.client.event;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class EventClientConfiguration {

    @Bean
    public ErrorDecoder eventClientErrorDecoder() {
        return new EventClientErrorDecoder();
    }
}
