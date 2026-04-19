package ru.practicum.ewm.api.client.user;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class UserClientConfiguration {

    @Bean
    public ErrorDecoder userClientErrorDecoder() {
        return new UserClientErrorDecoder();
    }
}