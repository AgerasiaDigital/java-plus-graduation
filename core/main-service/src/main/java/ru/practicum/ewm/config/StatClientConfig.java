package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.client.StatClient;
import ru.practicum.client.StatClientImpl;

@Configuration
public class StatClientConfig {

    @Bean
    public StatClient statClient(
            ObjectProvider<DiscoveryClient> discoveryClientProvider,
            @Value("${stats-server.url:}") String statsUrl,
            @Value("${stats.service-id:stats-server}") String statsServiceId) {
        if (statsUrl != null && !statsUrl.isBlank()) {
            return new StatClientImpl(statsUrl);
        }
        return new StatClientImpl(discoveryClientProvider.getObject(), statsServiceId);
    }
}
