package ru.practicum.ewm.analyzer.infrastructure.kafka.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.common.kafka.EventSimilarityAvroDeserializer;
import ru.practicum.ewm.stats.common.kafka.UserActionAvroDeserializer;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;
import ru.practicum.ewm.stats.kafka.UserActionAvro;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    // --- FACTORY FOR USER ACTIONS ---
    @Bean
    public ConsumerFactory<String, UserActionAvro> userActionConsumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionAvroDeserializer.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public KafkaListenerContainerFactory<?> actionContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userActionConsumerFactory());
        return factory;
    }

    // --- FACTORY FOR EVENT SIMILARITIES ---
    @Bean
    public ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityAvroDeserializer.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public KafkaListenerContainerFactory<?> similarityContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventSimilarityConsumerFactory());
        return factory;
    }
}