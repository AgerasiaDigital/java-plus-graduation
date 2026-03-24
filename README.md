# Explore With Me

Платформа для поиска событий и совместного досуга. Пользователи могут публиковать события, подавать заявки на участие, а администраторы — модерировать контент.

## Архитектура

Проект состоит из двух независимых микросервисов:

| Сервис | Порт | Описание |
|---|---|---|
| `main-service` | 8080 | Основная бизнес-логика: события, категории, подборки, заявки |
| `stat-server` | 9090 | Сбор и выдача статистики просмотров эндпоинтов |

Каждый сервис использует собственную базу данных PostgreSQL.

## Стек технологий

- **Java 21**
- **Spring Boot 3.3**
- **Spring Data JPA** + QueryDSL
- **PostgreSQL 16**
- **Docker / Docker Compose**
- **MapStruct** — маппинг сущностей
- **Lombok**

## Запуск

### Требования

- Docker и Docker Compose

### Запуск через Docker Compose

```bash
docker-compose up
```

После запуска:
- Main service: `http://localhost:8080`
- Stat server: `http://localhost:9090`

### Локальный запуск (для разработки)

```bash
mvn clean install -DskipTests
```

Профиль `test` использует встроенную H2-базу данных.

## API

### Main Service

Полная спецификация: `ewm-main-service-spec.json`

#### Публичный API
| Метод | Эндпоинт | Описание |
|---|---|---|
| GET | `/events` | Получение событий с фильтрацией |
| GET | `/events/{id}` | Получение события по ID |
| GET | `/categories` | Список категорий |
| GET | `/categories/{catId}` | Категория по ID |
| GET | `/compilations` | Список подборок |
| GET | `/compilations/{compId}` | Подборка по ID |

#### Приватный API (для авторизованных пользователей)
| Метод | Эндпоинт | Описание |
|---|---|---|
| GET | `/users/{userId}/events` | События пользователя |
| POST | `/users/{userId}/events` | Создание события |
| GET | `/users/{userId}/events/{eventId}` | Полная информация о событии |
| PATCH | `/users/{userId}/events/{eventId}` | Редактирование события |
| GET | `/users/{userId}/events/{eventId}/requests` | Заявки на участие в событии |
| PATCH | `/users/{userId}/events/{eventId}/requests` | Изменение статуса заявок |
| GET | `/users/{userId}/requests` | Заявки пользователя |
| POST | `/users/{userId}/requests` | Подача заявки на участие |
| PATCH | `/users/{userId}/requests/{requestId}/cancel` | Отмена заявки |

#### Admin API
| Метод | Эндпоинт | Описание |
|---|---|---|
| GET | `/admin/events` | Поиск событий |
| PATCH | `/admin/events/{eventId}` | Публикация / отклонение события |
| GET | `/admin/users` | Список пользователей |
| POST | `/admin/users` | Создание пользователя |
| DELETE | `/admin/users/{userId}` | Удаление пользователя |
| POST | `/admin/categories` | Создание категории |
| PATCH | `/admin/categories/{catId}` | Обновление категории |
| DELETE | `/admin/categories/{catId}` | Удаление категории |
| POST | `/admin/compilations` | Создание подборки |
| PATCH | `/admin/compilations/{compId}` | Обновление подборки |
| DELETE | `/admin/compilations/{compId}` | Удаление подборки |

### Stat Service

Полная спецификация: `ewm-stats-service-spec.json`

| Метод | Эндпоинт | Описание |
|---|---|---|
| POST | `/hit` | Сохранение информации о запросе |
| GET | `/stats` | Получение статистики по диапазону дат и URI |

## Жизненный цикл события

```
PENDING → PUBLISHED  (действие администратора: PUBLISH_EVENT)
PENDING → CANCELED   (действие администратора: REJECT_EVENT)
PENDING → CANCELED   (действие пользователя: CANCEL_REVIEW)
CANCELED → PENDING   (действие пользователя: SEND_TO_REVIEW)
```

Редактирование события пользователем доступно только в статусах `PENDING` и `CANCELED`, не позднее чем за 2 часа до начала.

## Структура проекта

```
explore-with-me/
├── main-service/          # Основной сервис
│   └── src/main/java/ru/practicum/ewm/
│       ├── controller/    # REST-контроллеры (Admin, Public, Private)
│       ├── event/         # Логика событий (сервис, репозиторий, фильтры)
│       ├── service/       # Сервисы: пользователи, категории, подборки, заявки
│       ├── model/         # JPA-сущности
│       ├── dto/           # DTO объекты
│       └── mapper/        # Маперы сущностей
├── stat/
│   ├── stat-dto/          # Общие DTO для клиента и сервера
│   ├── stat-client/       # HTTP-клиент для обращения к stat-server
│   └── stat-server/       # Сервис статистики
└── docker-compose.yml
```

## Тестирование

Коллекции Postman находятся в директории `postman/`.

CI-тесты запускаются автоматически при создании Pull Request (`.github/workflows/api-tests.yml`).
