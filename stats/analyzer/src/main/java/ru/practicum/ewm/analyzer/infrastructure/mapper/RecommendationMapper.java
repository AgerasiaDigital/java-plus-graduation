package ru.practicum.ewm.analyzer.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.ewm.analyzer.domain.Recommendation;
import ru.practicum.ewm.stats.grpc.RecommendedEventProto;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

    @Mappings({
        @Mapping(target = "mergeFrom", ignore = true),
        @Mapping(target = "clearField", ignore = true),
        @Mapping(target = "clearOneof", ignore = true),
        @Mapping(target = "unknownFields", ignore = true),
        @Mapping(target = "mergeUnknownFields", ignore = true),
        @Mapping(target = "allFields", ignore = true),
    })
    RecommendedEventProto toProto(Recommendation recommendation);
}
