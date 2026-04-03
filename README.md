# ExploreWithMe — микросервисная архитектура

Приложение для публикации и поиска мероприятий, реализованное в виде набора независимых микросервисов.

## Архитектура

```
                        ┌─────────────────┐
                        │  Gateway Server  │
                        │    :8080         │
                        └────────┬────────┘
                                 │ маршрутизация по path
              ┌──────────────────┼──────────────────┐
              │                  │                   │
    ┌─────────┴──────┐  ┌────────┴───────┐  ┌───────┴────────┐
    │  event-service │  │  user-service  │  │ request-service│
    │  (случайный    │  │  (случайный    │  │  (случайный    │
    │   порт)        │  │   порт)        │  │   порт)        │
    └────────────────┘  └────────────────┘  └────────────────┘
              │                                      │
    ┌─────────┴──────┐           ┌────────────────── ┘
    │compilation-svc │           │
    │  (случайный    │           │
    │   порт)        │           │
    └────────────────┘           │
                                 │
                    ┌────────────┴────────┐
                    │  Config Server      │  ← централизованная конфигурация
                    └────────────────────┘
                    ┌────────────────────┐
                    │  Discovery Server  │  ← Eureka :8761
                    └────────────────────┘
                    ┌────────────────────┐
                    │  Stat Server       │  ← статистика :9090
                    └────────────────────┘
```

## Микросервисы

### event-service
Управление событиями и категориями.

**Публичные API:**
- `GET /events` — список событий с фильтрацией
- `GET /events/{id}` — событие по id
- `GET /categories` — список категорий
- `GET /categories/{catId}` — категория по id

**Admin API:**
- `POST/PATCH/DELETE /admin/categories/**` — управление категориями
- `GET/PATCH /admin/events/**` — модерация событий

**Private API (через gateway):**
- `POST /users/{userId}/events` — создать событие
- `GET /users/{userId}/events` — события пользователя
- `GET/PATCH /users/{userId}/events/{eventId}` — получить/изменить событие

**Internal API (межсервисный, не маршрутизируется через gateway):**
- `GET /internal/events/{eventId}` — минимальная информация о событии для валидации в request-service
- `GET /internal/events?ids=...` — список EventShortDto для compilation-service

**Зависимости (OpenFeign):**
- `user-service` — получение данных об инициаторах событий
- `request-service` — получение количества подтверждённых заявок
- `stat-server` — регистрация просмотров, получение статистики

**База данных:** PostgreSQL `ewm` (порт 5433 в docker-compose)

---

### user-service
Администрирование пользователей.

**Admin API:**
- `GET /admin/users` — список пользователей
- `POST /admin/users` — создать пользователя
- `DELETE /admin/users/{userId}` — удалить пользователя

**Internal API (межсервисный):**
- `GET /internal/users/{userId}` — пользователь по id (для event-service)
- `GET /internal/users?ids=...` — Map<id, UserShortDto> пакетный запрос

**База данных:** PostgreSQL `users` (порт 5434 в docker-compose)

---

### request-service
Управление заявками на участие в событиях.

**Private API:**
- `GET /users/{userId}/requests` — заявки пользователя
- `POST /users/{userId}/requests` — подать заявку
- `PATCH /users/{userId}/requests/{requestId}/cancel` — отменить заявку
- `GET /users/{userId}/events/{eventId}/requests` — заявки на событие организатора
- `PATCH /users/{userId}/events/{eventId}/requests` — подтвердить/отклонить заявки

**Internal API (межсервисный):**
- `GET /internal/requests/count?eventIds=...` — Map<eventId, count> подтверждённых заявок

**Зависимости (OpenFeign):**
- `event-service` — валидация события при подаче заявки (лимит, статус, инициатор)

**База данных:** PostgreSQL `requests` (порт 5435 в docker-compose)

---

### compilation-service
Управление подборками событий.

**Публичные API:**
- `GET /compilations` — список подборок
- `GET /compilations/{compId}` — подборка по id

**Admin API:**
- `POST /admin/compilations` — создать подборку
- `PATCH /admin/compilations/{compId}` — изменить подборку
- `DELETE /admin/compilations/{compId}` — удалить подборку

**Зависимости (OpenFeign):**
- `event-service` — получение EventShortDto для событий в подборке

**База данных:** PostgreSQL `compilations` (порт 5436 в docker-compose)

---

### Инфраструктурные сервисы

| Сервис             | Порт | Назначение                                 |
|--------------------|------|--------------------------------------------|
| discovery-server   | 8761 | Eureka — регистрация и обнаружение сервисов |
| config-server      | авто | Spring Cloud Config, native profile         |
| gateway-server     | 8080 | Spring Cloud Gateway, точка входа           |
| stat-server        | 9090 | Сервис статистики просмотров                |

## Межсервисное взаимодействие

Все вызовы между сервисами используют **OpenFeign** с балансировкой нагрузки через Eureka (`lb://service-name`).

Для отказоустойчивости применяется **Resilience4j Circuit Breaker** с fallback-стратегиями:
- `event-service` → `request-service` недоступен: возвращает `0` подтверждённых заявок
- `event-service` → `user-service` недоступен: возвращает инициатора с именем `"Unavailable"`
- `compilation-service` → `event-service` недоступен: возвращает пустой список событий
- `request-service` → `event-service` недоступен: выбрасывает NotFoundException (нельзя подать заявку без валидации события)

## Маршрутизация Gateway

| Path                                  | Сервис              |
|---------------------------------------|---------------------|
| `/users/*/events/*/requests/**`       | request-service     |
| `/users/*/requests/**`                | request-service     |
| `/users/*/events/**`                  | event-service       |
| `/admin/events/**`                    | event-service       |
| `/events/**`                          | event-service       |
| `/admin/categories/**`                | event-service       |
| `/categories/**`                      | event-service       |
| `/admin/compilations/**`              | compilation-service |
| `/compilations/**`                    | compilation-service |
| `/admin/users/**`                     | user-service        |

## Запуск

```bash
docker-compose up --build
```

После запуска API доступно на `http://localhost:8080`.

Спецификация внешнего API: [ewm-main-service-spec.json](ewm-main-service-spec.json)
