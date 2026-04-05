package ru.practicum.collector.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.kafka.AvroBytesSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CollectorKafkaConfig {

    @Bean
    public ProducerFactory<String, UserActionAvro> userActionProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.remove(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
        props.remove(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
        return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                new AvroBytesSerializer<>(UserActionAvro.class));
    }

    @Bean
    public KafkaTemplate<String, UserActionAvro> userActionKafkaTemplate(
            ProducerFactory<String, UserActionAvro> userActionProducerFactory) {
        return new KafkaTemplate<>(userActionProducerFactory);
    }
}
