package ru.practicum.ewm.api.client.request;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class RequestClientConfiguration {

    @Bean
    public ErrorDecoder requestClientErrorDecoder() {
        return new RequestClientErrorDecoder();
    }
}
