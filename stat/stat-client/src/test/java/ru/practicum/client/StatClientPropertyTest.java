package ru.practicum.client;

// Feature: cloud-infrastructure, Property 1: Stats_Client resolves address via DiscoveryClient

import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsParamDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Validates: Requirements 2.8
 */
class StatClientPropertyTest {

    @Property(tries = 100)
    void statClientAlwaysUsesDiscoveryClientForHit(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String host,
            @ForAll @IntRange(min = 1024, max = 65535) int port) {

        // Arrange
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        RestClient.Builder restClientBuilder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class);
        ServiceInstance serviceInstance = mock(ServiceInstance.class);

        String url = "http://" + host + ":" + port;
        when(discoveryClient.getInstances("stat-server")).thenReturn(List.of(serviceInstance));
        when(serviceInstance.getUri()).thenReturn(URI.create(url));

        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        StatClientImpl statClient = new StatClientImpl(discoveryClient, restClientBuilder);
        EndpointHitDto dto = new EndpointHitDto("app", "/test", "127.0.0.1", LocalDateTime.now());

        // Act
        statClient.hit(dto);

        // Assert: DiscoveryClient.getInstances("stat-server") должен быть вызван
        verify(discoveryClient).getInstances("stat-server");
    }

    @Property(tries = 100)
    void statClientAlwaysUsesDiscoveryClientForGetStats(
            @ForAll @AlphaChars @StringLength(min = 1, max = 20) String host,
            @ForAll @IntRange(min = 1024, max = 65535) int port) {

        // Arrange
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        RestClient.Builder restClientBuilder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class);
        ServiceInstance serviceInstance = mock(ServiceInstance.class);

        String url = "http://" + host + ":" + port;
        when(discoveryClient.getInstances("stat-server")).thenReturn(List.of(serviceInstance));
        when(serviceInstance.getUri()).thenReturn(URI.create(url));

        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(java.util.function.Function.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.body(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(Collections.emptyList());

        StatClientImpl statClient = new StatClientImpl(discoveryClient, restClientBuilder);
        StatsParamDto params = new StatsParamDto(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/test"),
                false
        );

        // Act
        statClient.getStats(params);

        // Assert: DiscoveryClient.getInstances("stat-server") должен быть вызван
        verify(discoveryClient).getInstances("stat-server");
    }
}
