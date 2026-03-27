package ru.practicum.additional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import ru.practicum.ewm.MainApplication;
import ru.practicum.ewm.controller.*;
import ru.practicum.ewm.event.EventController;

@EnableFeignClients
@SpringBootApplication
@ComponentScan(
        basePackages = {"ru.practicum.additional", "ru.practicum.ewm"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                MainApplication.class,
                                UserController.class,
                                RequestController.class,
                                CategoryController.class,
                                AdminCategoryController.class,
                                CompilationController.class,
                                AdminCompilationController.class,
                                EventController.class
                        }
                )
        }
)
public class AdditionalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdditionalServiceApplication.class, args);
    }
}

