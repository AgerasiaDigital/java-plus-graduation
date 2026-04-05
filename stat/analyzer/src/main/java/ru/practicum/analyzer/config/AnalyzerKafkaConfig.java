package ru.practicum.analyzer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.kafka.AvroBytesDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AnalyzerKafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userActionsKafkaListenerFactory(
            KafkaProperties kafkaProperties) {
        return factory(kafkaProperties, UserActionAvro.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> similarityKafkaListenerFactory(
            KafkaProperties kafkaProperties) {
        return factory(kafkaProperties, EventSimilarityAvro.class);
    }

    private static <T extends org.apache.avro.specific.SpecificRecord> ConcurrentKafkaListenerContainerFactory<String, T> factory(
            KafkaProperties kafkaProperties,
            Class<T> valueClass) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        props.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<String, T> cf = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new AvroBytesDeserializer<>(valueClass));
        ConcurrentKafkaListenerContainerFactory<String, T> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(cf);
        return f;
    }
}
