# SmartThings Store

Микросервисная платформа интернет-магазина товаров для умного дома на Spring Boot / Spring Cloud с PostgreSQL, Redis, Docker и тестовым набором под требования ТЗ.

## Архитектура

Платформа состоит из сервисов:

- `config-server` — централизованная конфигурация сервисов из `config-repo`
- `discovery-server` — Service Registry на Eureka
- `api-gateway` — единая точка входа, JWT-проверка, role-check для admin-операций
- `user-service` — регистрация, логин, CRUD пользователей
- `product-service` — каталог товаров, фильтры, складской остаток, Redis-кэш каталога
- `order-service` — оформление заказов, история заказов, статусы, межсервисные вызовы и circuit breaker
- `notification-service` — журнал уведомлений о созданных заказах
- `frontend` — готовый шаблон витрины и базовой админской панели

## Стек

### Backend

- Java 23
- Gradle 8.10.2 Wrapper
- Spring Boot 3.4.2
- Spring Cloud 2024.0.1
- Spring Cloud Config
- Eureka
- Spring Cloud Gateway
- Spring Data JPA
- PostgreSQL
- Redis
- OpenFeign
- Resilience4j
- Springdoc OpenAPI / Swagger
- Spring Boot Actuator

### Frontend

- React 19
- Vite 7
- CSS без UI-библиотек

### Контейнеризация

- Docker
- Docker Compose
- Multi-stage Dockerfiles для каждого сервиса

## Почему сделано так

- `Config Server` и `Eureka` оставлены как обязательная часть микросервисной схемы из ТЗ, чтобы инфраструктура не была временной или условной.
- `API Gateway` забирает на себя единую авторизацию и базовые правила доступа, чтобы доменные сервисы не дублировали один и тот же входной контроль.
- `Database per service` сохранён: у каждого backend-сервиса своя PostgreSQL база.
- `Redis` используется в `product-service` как реальный внешний кэш для списка товаров, а не как локальная заглушка.
- `Frontend` оставлен простым и расширяемым: это рабочий шаблон, который легко переписать под ваш будущий дизайн.

## Структура проекта

- `common` — общие DTO, enum, исключения, JWT utility
- `config-server` — сервер конфигурации
- `discovery-server` — Eureka Registry
- `api-gateway` — gateway и JWT filter
- `user-service` — пользователи и авторизация
- `product-service` — каталог товаров и Redis cache
- `order-service` — заказы и отказоустойчивость
- `notification-service` — лог уведомлений
- `frontend` — React frontend
- `config-repo` — внешние YAML-конфиги сервисов
- `scripts/docker-smoke-test.ps1` — smoke-check контейнерного стенда
- `docker-compose.yml` — полный стек платформы

## Порты

- `5173` — frontend в Docker
- `8080` — api-gateway
- `8091` — user-service
- `8092` — product-service
- `8093` — order-service
- `8094` — notification-service
- `8761` — discovery-server
- `8888` — config-server
- `5433` — user PostgreSQL
- `5434` — product PostgreSQL
- `5435` — order PostgreSQL
- `5436` — notification PostgreSQL
- `6379` — Redis

## Демо-учётка

- email: `admin@smartthings.local`
- password: `admin123`

## Сборка

Backend:

```powershell
.\gradlew.bat build
```

Frontend:

```powershell
cd frontend
npm install
npm run build
```

## Запуск через Docker Compose

Полный стенд:

```powershell
docker compose up -d --build
```

Остановка:

```powershell
docker compose down
```

Остановка с удалением volume баз:

```powershell
docker compose down -v
```

Smoke-check после старта:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-smoke-test.ps1
```

## Что откроется после запуска Docker

- Frontend: [http://localhost:5173](http://localhost:5173)
- API Gateway: [http://localhost:8080](http://localhost:8080)
- Eureka: [http://localhost:8761](http://localhost:8761)

Swagger каждого backend-сервиса:

- [http://localhost:8091/swagger-ui.html](http://localhost:8091/swagger-ui.html)
- [http://localhost:8092/swagger-ui.html](http://localhost:8092/swagger-ui.html)
- [http://localhost:8093/swagger-ui.html](http://localhost:8093/swagger-ui.html)
- [http://localhost:8094/swagger-ui.html](http://localhost:8094/swagger-ui.html)

## Ручной запуск без полного Compose

Если хотите поднимать backend вручную, сначала нужен PostgreSQL и Redis. Проще всего поднять только инфраструктуру:

```powershell
docker compose up -d redis user-db product-db order-db notification-db
```

Потом из корня проекта в отдельных консолях:

```powershell
.\gradlew.bat :config-server:bootRun
```

```powershell
.\gradlew.bat :discovery-server:bootRun
```

```powershell
.\gradlew.bat :user-service:bootRun
```

```powershell
.\gradlew.bat :product-service:bootRun
```

```powershell
.\gradlew.bat :notification-service:bootRun
```

```powershell
.\gradlew.bat :order-service:bootRun
```

```powershell
.\gradlew.bat :api-gateway:bootRun
```

Frontend в dev-режиме:

```powershell
cd frontend
npm install
npm run dev
```

## Конфигурация

Все внешние конфиги находятся в `config-repo`.

Ключевые параметры:

- `USER_DB_URL`, `PRODUCT_DB_URL`, `ORDER_DB_URL`, `NOTIFICATION_DB_URL`
- `*_DB_USERNAME`, `*_DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`
- `CONFIG_SERVER_URL`
- `EUREKA_SERVER_URL`

По умолчанию локальные значения уже настроены на порты из `docker-compose.yml`.

## Что можно проверить вручную

Через frontend:

- просмотр каталога
- регистрация пользователя
- вход пользователя
- корзина
- оформление заказа
- просмотр заказов
- вход под admin
- добавление нового товара
- просмотр уведомлений

Через gateway API:

- `GET /api/products`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/orders`
- `GET /api/orders`
- `POST /api/products` под `ADMIN`
- `GET /api/notifications` под `ADMIN`

## Тесты

Запуск:

```powershell
.\gradlew.bat test
```

### Какие тесты подготовлены

#### Unit tests

Проверяют бизнес-логику service-слоя без поднятия всего приложения.

Что покрывают:

- регистрация и логин пользователя
- создание, обновление и удаление пользователя
- фильтрация каталога и резервирование товара
- расчёт заказа и изменение статуса
- fallback/ошибки при недоступности `product-service`
- запись и сортировку уведомлений

Зачем нужны:

- быстро ловят ошибки в бизнес-логике
- изолируют сервисный слой
- позволяют безопасно менять правила домена

#### Integration tests

Проверяют цепочку `Controller -> Service -> Repository -> DB`.

Что покрывают:

- создание и чтение пользователей
- создание и чтение товаров
- создание заказа и последующее обновление статуса
- запись и получение уведомлений

Зачем нужны:

- подтверждают, что web-слой и persistence-слой собраны корректно
- выявляют ошибки сериализации, маппинга и JPA-конфигурации

#### API tests

Проверяют HTTP статус-коды и JSON-ответы контроллеров.

Что покрывают:

- `/api/auth/register`
- `/api/auth/login`
- `/api/products`
- `/api/orders`
- `/api/notifications`

Зачем нужны:

- фиксируют контракт API
- помогают не ломать frontend и интеграции при изменениях backend

#### Fault tolerance tests

Проверяют fallback-логику `order-service` при сбоях каталога.

Что покрывают:

- fallback при чтении товара
- fallback при резервировании товара

Зачем нужны:

- подтверждают, что система деградирует контролируемо
- помогают проверить реализацию circuit breaker и user-friendly ошибок

#### Container smoke test

Проверяет, что после `docker compose up` поднимаются все сервисы и через gateway доступен каталог.

Запуск:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-smoke-test.ps1
```

Зачем нужен:

- даёт быструю проверку контейнерного стенда после изменений инфраструктуры
- нужен как базовая smoke-проверка перед дальнейшей подготовкой к CI/CD

## Что уже проверено

Фактически прогнано и успешно прошло:

- `.\gradlew.bat test`
- `.\gradlew.bat build`
- `npm run build`
- `docker compose config`
- `docker compose up -d --build`
- `scripts/docker-smoke-test.ps1`

## Что дальше

Следующий разумный этап после этой версии:

1. добавить GitHub Actions
2. подготовить Kubernetes manifests
3. усилить безопасность gateway и сервисов
4. вынести секреты и env-файлы
5. расширить доменную модель магазина

