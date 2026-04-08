package ru.practicum.ewm.api.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.api.error.ApiError;

@Slf4j
public class ErrorParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Optional<ApiError> parseErrorDto(Response response) {
        if (response.body() == null) {
            return Optional.empty();
        }
        try (InputStream bodyIs = response.body().asInputStream()) {
            return Optional.of(objectMapper.readValue(bodyIs, ApiError.class));
        } catch (IOException e) {
            log.error("Failed to decode error response body", e);
            return Optional.empty();
        }
    }

}
