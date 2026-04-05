package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.client.GrpcStatsClientsConfiguration;

@SpringBootApplication
@EnableFeignClients
@Import(GrpcStatsClientsConfiguration.class)
public class RequestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}
