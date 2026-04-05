package ru.practicum.ewm.stats.kafka;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public final class AvroBytesDeserializer<T extends SpecificRecord> implements Deserializer<T> {

    private final DatumReader<T> reader;

    public AvroBytesDeserializer(Class<T> clazz) {
        this.reader = new SpecificDatumReader<>(clazz);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
