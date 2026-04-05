package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcStatsClientsConfiguration {

    @Bean(destroyMethod = "shutdown")
    public CollectorUserActionGrpcClient collectorUserActionGrpcClient(
            DiscoveryClient discoveryClient,
            @Value("${ewm.grpc.collector-service-id:collector}") String serviceId) {
        return new CollectorUserActionGrpcClient(discoveryClient, serviceId);
    }

    @Bean(destroyMethod = "shutdown")
    public RecommendationsGrpcClient recommendationsGrpcClient(
            DiscoveryClient discoveryClient,
            @Value("${ewm.grpc.analyzer-service-id:analyzer}") String serviceId) {
        return new RecommendationsGrpcClient(discoveryClient, serviceId);
    }
}
