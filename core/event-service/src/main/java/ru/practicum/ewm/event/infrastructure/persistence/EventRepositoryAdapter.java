package ru.practicum.ewm.event.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.application.params.AdminEventSearchParams;
import ru.practicum.ewm.event.application.params.PublicEventSearchParams;
import ru.practicum.ewm.event.domain.Event;
import ru.practicum.ewm.event.domain.EventRepository;
import ru.practicum.ewm.event.domain.EventState;
import ru.practicum.ewm.event.domain.QEvent;

@Component
@RequiredArgsConstructor
public class EventRepositoryAdapter implements EventRepository {

    private final JpaEventRepository jpaEventRepository;

    @Override
    public Event save(Event event) {
        return jpaEventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(Long eventId) {
        return jpaEventRepository.findById(eventId);
    }

    @Override
    public Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId) {
        return jpaEventRepository.findByIdAndInitiatorId(eventId, initiatorId);
    }

    @Override
    public Optional<Event> findByIdAndState(Long eventId, EventState state) {
        return jpaEventRepository.findByIdAndState(eventId, state);
    }

    @Override
    public List<Event> findByInitiatorId(Long initiatorId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        return jpaEventRepository.findByInitiatorId(initiatorId, pageable).getContent();
    }

    @Override
    public List<Event> findPublic(PublicEventSearchParams params, int from, int size) {
        QEvent qEvent = QEvent.event;
        BooleanBuilder predicate = buildPublicPredicate(params, qEvent);
        Sort sortOption = Sort.by(Sort.Direction.ASC, "eventDate");
        Pageable pageable = PageRequest.of(from / size, size, sortOption);
        return jpaEventRepository.findAll(predicate, pageable).getContent();
    }

    @Override
    public List<Event> findAdmin(AdminEventSearchParams params, int from, int size) {
        QEvent qEvent = QEvent.event;
        BooleanBuilder predicate = buildAdminPredicate(params, qEvent);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        return jpaEventRepository.findAll(predicate, pageable).getContent();
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return jpaEventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public List<Event> findAllByIdIn(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }
        return jpaEventRepository.findAllById(eventIds);
    }

    private BooleanBuilder buildPublicPredicate(PublicEventSearchParams params, QEvent qEvent) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(qEvent.state.eq(EventState.PUBLISHED));

        if (params.getText() != null && !params.getText().isBlank()) {
            String searchText = params.getText().toLowerCase();
            predicate.and(qEvent.annotation.lower().like("%" + searchText + "%")
                .or(qEvent.description.lower().like("%" + searchText + "%")));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            predicate.and(qEvent.category.id.in(params.getCategories()));
        }
        if (params.getPaid() != null) {
            predicate.and(qEvent.paid.eq(params.getPaid()));
        }
        if (params.getRangeStart() == null && params.getRangeEnd() == null) {
            predicate.and(qEvent.eventDate.after(LocalDateTime.now()));
        } else {
            if (params.getRangeStart() != null) {
                predicate.and(qEvent.eventDate.goe(params.getRangeStart()));
            }
            if (params.getRangeEnd() != null) {
                predicate.and(qEvent.eventDate.loe(params.getRangeEnd()));
            }
        }
        return predicate;
    }

    private BooleanBuilder buildAdminPredicate(AdminEventSearchParams params, QEvent qEvent) {
        BooleanBuilder predicate = new BooleanBuilder();
        if (params.getUsers() != null && !params.getUsers().isEmpty()) {
            predicate.and(qEvent.initiatorId.in(params.getUsers()));
        }
        if (params.getStates() != null && !params.getStates().isEmpty()) {
            predicate.and(qEvent.state.in(params.getStates()));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            predicate.and(qEvent.category.id.in(params.getCategories()));
        }
        if (params.getRangeStart() != null) {
            predicate.and(qEvent.eventDate.goe(params.getRangeStart()));
        }
        if (params.getRangeEnd() != null) {
            predicate.and(qEvent.eventDate.loe(params.getRangeEnd()));
        }
        return predicate;
    }
}