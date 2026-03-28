package ru.practicum.ewm.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.client.CategoryClient;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.Location;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toNewEvent(NewEventDto dto, Long initiatorId) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategoryId(dto.getCategory());
        event.setDescription(dto.getDescription());
        event.setEventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER));
        event.setInitiator(initiatorId);
        event.setLocation(toLocation(dto.getLocation()));
        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);
        event.setTitle(dto.getTitle());
        return event;
    }

    public static Location toLocation(LocationDto dto) {
        if (dto == null) return null;
        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }

    public static LocationDto toLocationDto(Location location) {
        if (location == null) return null;
        LocationDto dto = new LocationDto();
        dto.setLat(location.getLat());
        dto.setLon(location.getLon());
        return dto;
    }

    public static EventFullDto toFullDto(Event event, CategoryClient categoryClient, UserClient userClient,
                                          Integer confirmedRequests, Long views) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());

        if (categoryClient != null) {
            try {
                dto.setCategory(categoryClient.getCategoryById(event.getCategoryId()));
            } catch (Exception e) {
                CategoryDto categoryDto = new CategoryDto();
                categoryDto.setId(event.getCategoryId());
                categoryDto.setName("Unknown");
                dto.setCategory(categoryDto);
            }
        }

        dto.setConfirmedRequests(confirmedRequests != null ? confirmedRequests : 0);
        dto.setCreatedOn(event.getCreatedOn());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());

        if (userClient != null) {
            try {
                UserDto userDto = userClient.getUserById(event.getInitiator());
                UserShortDto userShortDto = new UserShortDto();
                userShortDto.setId(userDto.getId());
                userShortDto.setName(userDto.getName());
                dto.setInitiator(userShortDto);
            } catch (Exception e) {
                UserShortDto userShortDto = new UserShortDto();
                userShortDto.setId(event.getInitiator());
                userShortDto.setName("Unknown");
                dto.setInitiator(userShortDto);
            }
        }

        dto.setLocation(toLocationDto(event.getLocation()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState().name());
        dto.setTitle(event.getTitle());
        dto.setViews(views != null ? views : 0L);

        return dto;
    }

    public static EventShortDto toShortDto(Event event, CategoryClient categoryClient, UserClient userClient,
                                            Integer confirmedRequests, Long views) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());

        if (categoryClient != null) {
            try {
                dto.setCategory(categoryClient.getCategoryById(event.getCategoryId()));
            } catch (Exception e) {
                CategoryDto categoryDto = new CategoryDto();
                categoryDto.setId(event.getCategoryId());
                categoryDto.setName("Unknown");
                dto.setCategory(categoryDto);
            }
        }

        dto.setConfirmedRequests(confirmedRequests != null ? confirmedRequests : 0);
        dto.setEventDate(event.getEventDate());

        if (userClient != null) {
            try {
                UserDto userDto = userClient.getUserById(event.getInitiator());
                UserShortDto userShortDto = new UserShortDto();
                userShortDto.setId(userDto.getId());
                userShortDto.setName(userDto.getName());
                dto.setInitiator(userShortDto);
            } catch (Exception e) {
                UserShortDto userShortDto = new UserShortDto();
                userShortDto.setId(event.getInitiator());
                userShortDto.setName("Unknown");
                dto.setInitiator(userShortDto);
            }
        }

        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(views != null ? views : 0L);

        return dto;
    }

    public static void updateFields(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategoryId(request.getCategory());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(request.getEventDate(), FORMATTER));
        }
        if (request.getLocation() != null) {
            event.setLocation(toLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }
}
