package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsParamDto;
import ru.practicum.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatClientImpl implements StatClient {
    private final DiscoveryClient discoveryClient;
    private final RestClient.Builder restClientBuilder;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatClientImpl(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
        this.discoveryClient = discoveryClient;
        this.restClientBuilder = restClientBuilder;
    }

    private String resolveStatServerUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("stat-server");
        if (instances.isEmpty()) {
            log.error("stat-server не найден в Eureka");
            throw new IllegalStateException("stat-server не найден в Eureka");
        }
        ServiceInstance instance = instances.get(0);
        return instance.getUri().toString();
    }

    @Override
    public void hit(EndpointHitDto endpointHitDto) {
        try {
            String url = resolveStatServerUrl();
            restClientBuilder.baseUrl(url).build()
                    .post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHitDto)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Stat-client отправил статистику: {}", endpointHitDto);
        } catch (Exception e) {
            log.error("Ошибка при отправке статистики: {}", e.getMessage());
        }
    }

    @Override
    public List<ViewStatsDto> getStats(StatsParamDto statsParamDto) {
        log.info("Запрос статистики для uri: {}", statsParamDto.getUris());
        try {
            String url = resolveStatServerUrl();
            List<ViewStatsDto> stats = restClientBuilder.baseUrl(url).build()
                    .get()
                    .uri(uriBuilder -> {
                        String startEncoded = URLEncoder.encode(
                                statsParamDto.getStart().format(formatter),
                                StandardCharsets.UTF_8
                        );
                        String endEncoded = URLEncoder.encode(
                                statsParamDto.getEnd().format(formatter),
                                StandardCharsets.UTF_8
                        );
                        uriBuilder.path("/stats")
                                .queryParam("start", startEncoded)
                                .queryParam("end", endEncoded);
                        if (statsParamDto.getUris() != null && !statsParamDto.getUris().isEmpty()) {
                            uriBuilder.queryParam("uris", statsParamDto.getUris());
                        }
                        if (statsParamDto.getIsUnique() != null) {
                            uriBuilder.queryParam("unique", statsParamDto.getIsUnique());
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (stats == null) {
                return Collections.emptyList();
            }
            return stats;
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
