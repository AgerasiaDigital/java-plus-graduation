package ru.practicum.ewm.stats.common.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class AvroSerializer<T extends SpecificRecordBase> implements Serializer<T> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No-op
    }

    @Override
    public byte[] serialize(String topic, T payload) {
        if (payload == null) {
            return null;
        }

        SpecificDatumWriter<GenericRecord> datumWriter = new SpecificDatumWriter<>(payload.getSchema());
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            datumWriter.write(payload, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error serializing Avro message for topic {}: {}", topic, payload, e);
            throw new SerializationException("Error serializing Avro message", e);
        }
    }

    @Override
    public void close() {
        // No-op
    }
}