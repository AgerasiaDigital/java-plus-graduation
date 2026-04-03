package ru.practicum.client.grpc;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class StatsGrpcClientAutoConfiguration {

    @Bean
    @ConditionalOnProperty("grpc.client.collector.address")
    public CollectorGrpcClient collectorGrpcClient() {
        return new CollectorGrpcClient();
    }

    @Bean
    @ConditionalOnProperty("grpc.client.analyzer.address")
    public AnalyzerGrpcClient analyzerGrpcClient() {
        return new AnalyzerGrpcClient();
    }
}
