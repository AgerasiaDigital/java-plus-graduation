package ru.practicum.aggregator.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.kafka.AvroBytesDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AggregatorKafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> kafkaListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        props.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<String, UserActionAvro> cf = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new AvroBytesDeserializer<>(UserActionAvro.class));
        ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(cf);
        return f;
    }
}
