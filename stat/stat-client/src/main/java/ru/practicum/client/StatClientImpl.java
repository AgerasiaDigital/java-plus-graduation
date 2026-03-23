package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
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
    private final RestClient restClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatClientImpl(String statUrl) {
        log.info("Stat-client использует url: {}", statUrl);
        restClient = RestClient.builder()
                .baseUrl(statUrl)
                .build();
    }

    @Override
    public void hit(EndpointHitDto endpointHitDto) {
        try {
            restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHitDto)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Stat-client отправил статистику по url: {}", endpointHitDto);
        } catch (Exception e) {
            log.error("Ошибка при отправке статистики: {}", e.getMessage());
        }
    }

    @Override
    public List<ViewStatsDto> getStats(StatsParamDto statsParamDto) {
        log.info("Запрос статистики для uri: {}", statsParamDto.getUris());
        log.debug("statsParamDto: {}", statsParamDto);

        try {
            List<ViewStatsDto> stats = restClient.get()
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
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (stats == null) {
                log.warn("Отсутствует статистика посещений для: {}", statsParamDto);
                return Collections.emptyList();
            }

            log.debug("Выгружена статистика: {}", stats);
            return stats;
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}