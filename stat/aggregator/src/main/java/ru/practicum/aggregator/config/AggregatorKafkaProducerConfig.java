package ru.practicum.aggregator.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.kafka.AvroBytesSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AggregatorKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, EventSimilarityAvro> eventSimilarityProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.remove(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
        props.remove(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
        return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                new AvroBytesSerializer<>(EventSimilarityAvro.class));
    }

    @Bean
    public KafkaTemplate<String, EventSimilarityAvro> eventSimilarityKafkaTemplate(
            ProducerFactory<String, EventSimilarityAvro> eventSimilarityProducerFactory) {
        return new KafkaTemplate<>(eventSimilarityProducerFactory);
    }
}
