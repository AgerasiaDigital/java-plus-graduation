package ru.practicum.ewm.stats.kafka;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class AvroBytesSerializer<T extends SpecificRecord> implements Serializer<T> {

    private final DatumWriter<T> writer;

    public AvroBytesSerializer(Class<T> clazz) {
        this.writer = new SpecificDatumWriter<>(clazz);
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
