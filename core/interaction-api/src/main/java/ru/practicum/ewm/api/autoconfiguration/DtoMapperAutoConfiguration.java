package ru.practicum.ewm.api.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.ewm.api.utility.DtoMapper;

@AutoConfiguration
@ConditionalOnClass(DtoMapper.class)
@ComponentScan(basePackageClasses = DtoMapper.class)
public class DtoMapperAutoConfiguration {

}
