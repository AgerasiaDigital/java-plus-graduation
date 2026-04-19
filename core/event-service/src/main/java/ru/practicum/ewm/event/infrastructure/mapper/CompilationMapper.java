package ru.practicum.ewm.event.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.ewm.api.client.event.dto.CompilationDto;
import ru.practicum.ewm.api.client.event.dto.NewCompilationDto;
import ru.practicum.ewm.api.client.event.dto.UpdateCompilationRequestDto;
import ru.practicum.ewm.event.domain.Compilation;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto newCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateCompilationFromDto(UpdateCompilationRequestDto dto, @MappingTarget Compilation compilation);
}