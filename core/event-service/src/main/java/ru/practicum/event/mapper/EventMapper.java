package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", source = "userId")
    @Mapping(target = "location", source = "newEventDto.location")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", source = "newEventDto.category", qualifiedByName = "categoryFromId")
    Event toEvent(NewEventDto newEventDto, Long userId);

    @Named("categoryFromId")
    default Category categoryFromId(Long id) {
        if (id == null) return null;
        Category category = new Category();
        category.setId(id);
        return category;
    }

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "confirmedRequests", source = "requests")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "annotation", source = "event.annotation")
    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "eventDate", source = "event.eventDate")
    @Mapping(target = "paid", source = "event.paid")
    @Mapping(target = "title", source = "event.title")
    EventShortDto toShortDto(Event event, Long requests, Long views, UserShortDto initiator);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "confirmedRequests", source = "requests")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "annotation", source = "event.annotation")
    @Mapping(target = "category", source = "event.category")
    @Mapping(target = "createdOn", source = "event.createdOn")
    @Mapping(target = "description", source = "event.description")
    @Mapping(target = "eventDate", source = "event.eventDate")
    @Mapping(target = "paid", source = "event.paid")
    @Mapping(target = "participantLimit", source = "event.participantLimit")
    @Mapping(target = "publishedOn", source = "event.publishedOn")
    @Mapping(target = "requestModeration", source = "event.requestModeration")
    @Mapping(target = "state", source = "event.state")
    @Mapping(target = "title", source = "event.title")
    EventFullDto toFullDto(Event event, Long requests, Long views, UserShortDto initiator);

    Location toLocation(LocationDto locationDto);

    default CategoryDto toCategoryDto(Category category) {
        if (category == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
