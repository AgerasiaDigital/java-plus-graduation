package ru.practicum.ewm.stats.client.autoconfiguration;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import ru.practicum.ewm.stats.client.AnalyzerClient;
import ru.practicum.ewm.stats.client.CollectorClient;
import ru.practicum.ewm.stats.client.aop.UserActionAspect;

@AutoConfiguration
@ConditionalOnClass(GrpcClient.class)
public class CollectorClientAutoconfiguration {

    @Configuration
    static class ClientBeans {
        @Bean
        public CollectorClient collectorClient() {
            return new CollectorClient();
        }

        @Bean
        public AnalyzerClient analyzerClient() {
            return new AnalyzerClient();
        }

        @Bean
        public ParameterNameDiscoverer parameterNameDiscoverer() {
            return new DefaultParameterNameDiscoverer();
        }
    }

    @Configuration
    static class AopConfiguration {

        @Bean
        public UserActionAspect userActionAspect(CollectorClient collectorClient, ParameterNameDiscoverer discoverer) {
            return new UserActionAspect(collectorClient, discoverer);
        }
    }
}
