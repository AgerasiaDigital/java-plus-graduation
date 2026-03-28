# ExploreWithMe — Stage 2: Microservices

## Architecture

### Infrastructure (`infra/`)

| Service | Port | Description |
|---------|------|-------------|
| discovery-server | 8761 | Eureka — service registry and discovery |
| config-server | dynamic (0) | Spring Cloud Config — centralized configuration |
| gateway-server | 8080 | Spring Cloud Gateway — single entry point for all clients |

Configurations for all services: `infra/config-server/src/main/resources/config/`

### Business services (`core/`)

| Service | Responsibility | Database |
|---------|---------------|----------|
| main-service | Legacy monolith (being phased out) | ewm-main |
| category-service | Category management | ewm-category |
| user-service | User management | ewm-user |
| event-service | Event management | ewm-event |
| request-service | Participation requests | ewm-request |
| compilation-service | Event compilations | ewm-compilation |

### Statistics (`stat/`)

| Service | Port | Description |
|---------|------|-------------|
| stats-server | 9090 | Hit collection and view statistics |

## Service Interaction

All external requests arrive at **gateway-server (port 8080)**, which routes them to the appropriate microservice.

Inter-service communication uses **OpenFeign + Eureka**:

- `event-service` → `category-service` (`GET /categories/{catId}`) — validate category on event creation
- `event-service` → `user-service` (`GET /admin/users/{userId}`) — resolve initiator data
- `request-service` → `event-service` (`GET /events/{eventId}`) — get event for participation check
- `request-service` → `user-service` (`GET /admin/users/{userId}`) — verify user exists
- `compilation-service` → `event-service` (`GET /internal/events/by-ids`) — fetch events by ids
- `category-service` → `event-service` (`GET /internal/events/exists-by-category`) — check events before category deletion

## Fault Tolerance

**Resilience4j CircuitBreaker + Retry** is configured on all Feign clients. When a dependency is unavailable, each client returns a sensible fallback value (empty list, false, etc.) so the calling service can continue processing requests.

CircuitBreaker settings (per dependency):
- `slidingWindowSize` = 5
- `failureRateThreshold` = 50%
- `waitDurationInOpenState` = 10s
- `maxAttempts` (retry) = 3

## Internal API

### event-service

| Method | Path | Description | Used by |
|--------|------|-------------|---------|
| GET | `/internal/events/by-ids?ids=1,2,3` | Get events by id list | compilation-service |
| GET | `/internal/events/exists-by-category?categoryId=1` | Check events exist in category | category-service |

### user-service

| Method | Path | Description | Used by |
|--------|------|-------------|---------|
| GET | `/admin/users/{userId}` | Get user by id | event-service, request-service |

## External API

Full specification: [ewm-main-service-spec.json](ewm-main-service-spec.json)

All requests must be sent to port **8080** (gateway-server).
