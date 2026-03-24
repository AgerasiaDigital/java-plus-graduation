package ru.practicum.statserver.server;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.statserver.model.StatMapper;
import ru.practicum.statserver.model.StatModel;
import ru.practicum.statserver.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Service
public class StatServiceImpl implements StatService {
    private StatRepository statRepository;

    @Transactional
    public void create(EndpointHitDto endpointHitDto) {
        StatModel statEntity = statRepository.save(StatMapper.createStatModel(endpointHitDto));
    }

    public List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end,
                                      Collection<String> uris, Boolean unique) {
        if (uris == null || uris.isEmpty()) {
            // Без фильтра по uris - отдельный запрос
            return statRepository.getStatWithoutUris(start, end, unique);
        }
        return statRepository.getStat(start, end, uris, unique);
    }
}
