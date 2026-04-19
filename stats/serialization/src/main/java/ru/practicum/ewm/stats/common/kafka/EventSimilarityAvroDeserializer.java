package ru.practicum.ewm.stats.common.kafka;

import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.kafka.EventSimilarityAvro;

@Slf4j
public class EventSimilarityAvroDeserializer implements Deserializer<EventSimilarityAvro> {

    private final SpecificDatumReader<EventSimilarityAvro> datumReader = new SpecificDatumReader<>(
        EventSimilarityAvro.getClassSchema());

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No-op
    }

    @Override
    public EventSimilarityAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return datumReader.read(null, decoder);
        } catch (IOException e) {
            log.error("Error deserializing Avro message for topic {}", topic, e);
            throw new SerializationException("Error deserializing Avro message", e);
        }
    }

    @Override
    public void close() {
        // No-op
    }
}
