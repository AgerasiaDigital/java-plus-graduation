package ru.practicum.ewm.stats.avro.serializer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventSimilarityAvroDeserializer implements Deserializer<EventSimilarityAvro> {

    private final SpecificDatumReader<EventSimilarityAvro> reader =
            new SpecificDatumReader<>(EventSimilarityAvro.getClassSchema());

    @Override
    public EventSimilarityAvro deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing EventSimilarityAvro from topic " + topic, e);
        }
    }
}
