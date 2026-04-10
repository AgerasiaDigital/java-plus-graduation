package ru.practicum.ewm.stats.common.kafka;

import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.kafka.UserActionAvro;

@Slf4j
public class UserActionAvroDeserializer implements Deserializer<UserActionAvro> {

    private final SpecificDatumReader<UserActionAvro> datumReader = new SpecificDatumReader<>(
        UserActionAvro.getClassSchema());

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No-op
    }

    @Override
    public UserActionAvro deserialize(String topic, byte[] data) {
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