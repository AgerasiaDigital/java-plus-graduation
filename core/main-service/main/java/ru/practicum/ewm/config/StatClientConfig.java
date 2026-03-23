package ru.practicum.ewm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.practicum.client.StatClient;
import ru.practicum.client.StatClientImpl;

@Slf4j
@Configuration
public class StatClientConfig {

    private static final String STATS_SERVICE_ID = "stats-server";

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean
    public StatClient statClient(DiscoveryClient discoveryClient, RetryTemplate retryTemplate) {
        return new DiscoveryAwareStatClient(discoveryClient, retryTemplate, STATS_SERVICE_ID);
    }

    static class DiscoveryAwareStatClient extends StatClientImpl {

        private final DiscoveryClient discoveryClient;
        private final RetryTemplate retryTemplate;
        private final String statsServiceId;

        DiscoveryAwareStatClient(DiscoveryClient discoveryClient,
                                 RetryTemplate retryTemplate,
                                 String statsServiceId) {
            super("http://stats-server");
            this.discoveryClient = discoveryClient;
            this.retryTemplate = retryTemplate;
            this.statsServiceId = statsServiceId;
        }

        private ServiceInstance getInstance() {
            try {
                return discoveryClient
                        .getInstances(statsServiceId)
                        .getFirst();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId, e);
            }
        }

        @Override
        protected String resolveBaseUrl() {
            return retryTemplate.execute(ctx -> {
                ServiceInstance instance = getInstance();
                String url = "http://" + instance.getHost() + ":" + instance.getPort();
                log.info("Resolved stats-server URL: {}", url);
                return url;
            });
        }
    }
}