package ru.practicum.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsParamDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatClientImplTest {

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private ServiceInstance serviceInstance;

    private StatClientImpl statClient;

    @BeforeEach
    void setUp() {
        statClient = new StatClientImpl(discoveryClient, restClientBuilder);
    }

    @Test
    void hit_callsDiscoveryClientGetInstances() {
        // Arrange
        when(discoveryClient.getInstances("stat-server")).thenReturn(List.of(serviceInstance));
        when(serviceInstance.getUri()).thenReturn(URI.create("http://localhost:9090"));

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

        EndpointHitDto dto = new EndpointHitDto("app", "/test", "127.0.0.1", LocalDateTime.now());

        // Act
        statClient.hit(dto);

        // Assert
        verify(discoveryClient).getInstances("stat-server");
    }

    @Test
    void getStats_callsDiscoveryClientGetInstances() {
        // Arrange
        when(discoveryClient.getInstances("stat-server")).thenReturn(List.of(serviceInstance));
        when(serviceInstance.getUri()).thenReturn(URI.create("http://localhost:9090"));

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
        when(responseSpec.body(any(org.springframework.core.ParameterizedTypeReference.class))).thenReturn(List.of());

        StatsParamDto params = new StatsParamDto(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/test"),
                false
        );

        // Act
        List<ViewStatsDto> result = statClient.getStats(params);

        // Assert
        verify(discoveryClient).getInstances("stat-server");
        assertNotNull(result);
    }

    @Test
    void getStats_returnsEmptyList_whenNoInstances() {
        // Arrange
        when(discoveryClient.getInstances("stat-server")).thenReturn(Collections.emptyList());

        StatsParamDto params = new StatsParamDto(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                List.of("/test"),
                false
        );

        // Act
        List<ViewStatsDto> result = statClient.getStats(params);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void hit_doesNotThrow_whenNoInstances() {
        // Arrange
        when(discoveryClient.getInstances("stat-server")).thenReturn(Collections.emptyList());

        EndpointHitDto dto = new EndpointHitDto("app", "/test", "127.0.0.1", LocalDateTime.now());

        // Act & Assert
        assertDoesNotThrow(() -> statClient.hit(dto));
    }
}
