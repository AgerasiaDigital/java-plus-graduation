package ru.practicum.ewm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.client.StatClient;
import ru.practicum.client.StatClientDiscoveryImpl;
import ru.practicum.client.StatClientImpl;

@Configuration
public class StatClientConfig {

    @Bean
    @ConditionalOnExpression("!'${stats-server.url:}'.isEmpty()")
    public StatClient statClientByStaticUrl(@Value("${stats-server.url:}") String statsUrl) {
        return new StatClientImpl(statsUrl);
    }

    @Bean
    @ConditionalOnExpression("'${stats-server.url:}'.isEmpty()")
    public StatClient statClientByDiscovery(
            DiscoveryClient discoveryClient,
            @Value("${stats.service-id:stats-server}") String statsServiceId) {
        return new StatClientDiscoveryImpl(discoveryClient, statsServiceId);
    }
}
