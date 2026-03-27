# Explore With Me (EWM) — Этап 2: разбиение на микросервисы

## Выделенные сервисы

В проекте на этапе 2 функциональность монолита постепенно разделяется на следующие микросервисы:

- `events-service` — управление событиями (эндпоинты `/events/**`, `/admin/events/**`, а также создание/редактирование событий).
- `requests-service` — управление заявками на участие (эндпоинты `/users/**/requests`, `/users/**/events/**/requests`).
- `users-admin-service` — администрирование пользователями (эндпоинты `/admin/users/**`).
- `additional-service` — доп. функциональность:
  - категории: `/categories/**`, `/admin/categories/**`
  - подборки: `/compilations/**`, `/admin/compilations/**`

Инфраструктурные компоненты:

- `gateway-server` — API Gateway (единая точка входа на `8080`).
- `discovery-server` — Eureka Server (регистрация сервисов и discovery).
- `config-server` — Spring Cloud Config Server (централизованная выдача конфигураций).
- `stats-server` — сервис статистики (обработка хитов `/hit` и выдача `/stats`).

## Взаимодействие сервисов

Для межсервисного взаимодействия используется:

- `Eureka` (service discovery)
- `OpenFeign` (клиенты для вызовов REST между сервисами)

Ключевой пример (используется в логике подсчёта `confirmedRequests`):

- `events-service` через Feign вызывает внутренний эндпоинт `requests-service` для подсчета подтвержденных заявок:
  - `POST /internal/requests/confirmed-counts`
    - Request body: `List<Long> eventIds`
    - Response: `Map<Long, Long>` (eventId -> confirmedRequests)

## Внутренний API (для Feign-взаимодействия)

Внутренние эндпоинты, доступные сервисам:

- `requests-service`
  - `POST /internal/requests/confirmed-counts`
- `events-service`
  - `GET /internal/events/{eventId}/request-rules`
  - `GET /internal/events/exists-by-category/{categoryId}`

## Конфигурация сервисов

Основная централизованная конфигурация находится в `infra/config-server`:

- `infra/config-server/src/main/resources/config/`
  - `events-service.properties`
  - `requests-service.properties`
  - `users-admin-service.properties`
  - `additional-service.properties`
  - `gateway-server.properties`
  - `main-service.properties` (оставшийся сервис/контекст)

Настройка маршрутизации в API Gateway:

- `infra/config-server/src/main/resources/config/gateway-server.properties`

## Внешний API

Внешняя спецификация API (эндпоинты проекта до/после разбиения сохраняют контракты):

- `ewm-main-service-spec.json` — [OpenAPI спецификация главного API](./ewm-main-service-spec.json)

Для статистики:

- `ewm-stats-service-spec.json` — [OpenAPI спецификация статистики](./ewm-stats-service-spec.json)

