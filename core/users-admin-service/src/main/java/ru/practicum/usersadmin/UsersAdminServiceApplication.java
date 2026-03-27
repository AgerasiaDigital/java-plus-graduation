package ru.practicum.usersadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import ru.practicum.ewm.MainApplication;
import ru.practicum.ewm.controller.AdminCategoryController;
import ru.practicum.ewm.controller.AdminCompilationController;
import ru.practicum.ewm.controller.CategoryController;
import ru.practicum.ewm.controller.CompilationController;
import ru.practicum.ewm.controller.RequestController;
import ru.practicum.ewm.controller.UserController;
import ru.practicum.ewm.event.EventController;

@SpringBootApplication
@ComponentScan(
        basePackages = {"ru.practicum.usersadmin", "ru.practicum.ewm"},
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
public class UsersAdminServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsersAdminServiceApplication.class, args);
    }
}

