package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.client.GrpcStatsClientsConfiguration;

@SpringBootApplication
@EnableFeignClients
@Import(GrpcStatsClientsConfiguration.class)
public class EventServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
