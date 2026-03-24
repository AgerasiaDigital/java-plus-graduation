package ru.practicum.ewm.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.client.StatClient;
import ru.practicum.client.StatClientImpl;

@Configuration
public class StatClientConfig {

    @Bean
    public StatClient statClient(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
        return new StatClientImpl(discoveryClient, restClientBuilder);
    }
}
