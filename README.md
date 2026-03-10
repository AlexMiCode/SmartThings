# SmartThings Store

Микросервисный интернет-магазин товаров для умного дома на Spring Boot / Spring Cloud. Проект закрывает требования ТЗ по backend-архитектуре, PostgreSQL, Redis, Docker, Kubernetes, Prometheus, CI/CD и тестированию.

## Состав системы

- `config-server` — централизованная конфигурация из `config-repo`
- `discovery-server` — service registry на Eureka
- `api-gateway` — единая точка входа, JWT-проверка, role-check для admin-эндпоинтов
- `user-service` — регистрация, логин, CRUD пользователей
- `product-service` — каталог товаров, фильтрация, управление остатками, Redis-кэш
- `order-service` — оформление заказов, статусы, вызовы других сервисов, circuit breaker
- `notification-service` — журнал уведомлений о заказах
- `frontend` — React SPA с публичной витриной, кабинетом и админкой
- `monitoring/prometheus.yml` — конфигурация Prometheus для Docker Compose
- `k8s` — Kubernetes-манифесты платформы
- `.github/workflows/ci-cd.yml` — GitHub Actions pipeline

## Технологии

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
- Spring Boot Actuator
- Micrometer Prometheus Registry
- Springdoc OpenAPI / Swagger

### Frontend

- React 19
- Vite 7
- React Router
- CSS без UI-библиотек

### Infrastructure

- Docker
- Docker Compose
- Kubernetes
- Prometheus
- GitHub Actions

## Почему архитектура сделана так

- `Config Server` и `Eureka` сохранены как полноценная часть микросервисной схемы, а не как временная заглушка.
- `API Gateway` централизует входной контроль и не размазывает JWT-логику по доменным сервисам.
- `Database per service` соблюдён: у каждого backend-сервиса своя PostgreSQL база.
- `Redis` используется как реальный внешний кэш каталога.
- `Prometheus` подключён через Actuator и Micrometer, чтобы метрики были доступны одинаково в Docker и Kubernetes.
- `Kubernetes` описан обычными манифестами без Helm, чтобы структура была прозрачной и легко защищаемой на показе проекта.
- `Frontend` специально оставлен как рабочий, но простой шаблон, который можно дальше свободно переделывать.

## Структура проекта

- `common` — общие DTO, enum, исключения, JWT utility
- `config-server`
- `discovery-server`
- `api-gateway`
- `user-service`
- `product-service`
- `order-service`
- `notification-service`
- `frontend`
- `config-repo`
- `monitoring`
- `k8s`
- `scripts`

## Порты

### Application

- `5173` — frontend в Docker Compose
- `8080` — API Gateway
- `8091` — user-service
- `8092` — product-service
- `8093` — order-service
- `8094` — notification-service
- `8761` — discovery-server
- `8888` — config-server

### Infrastructure

- `5433` — user PostgreSQL
- `5434` — product PostgreSQL
- `5435` — order PostgreSQL
- `5436` — notification PostgreSQL
- `6379` — Redis
- `9090` — Prometheus

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

Остановка с удалением volume:

```powershell
docker compose down -v
```

Smoke-check контейнерного стенда:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-smoke-test.ps1
```

После запуска доступны:

- Frontend: [http://localhost:5173](http://localhost:5173)
- API Gateway: [http://localhost:8080](http://localhost:8080)
- Eureka: [http://localhost:8761](http://localhost:8761)
- Prometheus: [http://localhost:9090](http://localhost:9090)

Swagger:

- [http://localhost:8091/swagger-ui.html](http://localhost:8091/swagger-ui.html)
- [http://localhost:8092/swagger-ui.html](http://localhost:8092/swagger-ui.html)
- [http://localhost:8093/swagger-ui.html](http://localhost:8093/swagger-ui.html)
- [http://localhost:8094/swagger-ui.html](http://localhost:8094/swagger-ui.html)

## Ручной запуск без полного Compose

Сначала поднимите только инфраструктуру:

```powershell
docker compose up -d redis prometheus user-db product-db order-db notification-db
```

Затем в отдельных консолях:

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

## Мониторинг

Во всех backend-сервисах подключены:

- `actuator/health`
- `actuator/metrics`
- `actuator/prometheus`

Prometheus собирает метрики со всех backend-компонентов через конфиг в [monitoring/prometheus.yml](C:\Users\lehax\OneDrive\Documents\SmartThings\monitoring\prometheus.yml).

## Kubernetes

Манифесты лежат в каталоге [k8s](C:\Users\lehax\OneDrive\Documents\SmartThings\k8s).

Что входит:

- `Namespace`
- `ConfigMap` с конфигами Config Server
- `Secret` с JWT и DB-учётными данными
- `Deployment` и `Service` для всех сервисов, PostgreSQL, Redis и Prometheus
- `Ingress` для frontend и gateway
- `HPA` для `product-service`
- readiness/liveness probes
- rolling update стратегия для `product-service`

### Перед деплоем в Kubernetes

1. Соберите и опубликуйте Docker-образы.
2. При необходимости замените имена образов `smartthings/...:latest` в манифестах на свои registry tags.
3. Убедитесь, что в кластере установлен ingress controller.
4. Для HPA нужен Metrics Server.

### Деплой

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy-k8s.ps1
```

Либо вручную:

```powershell
kubectl apply -f .\k8s
```

### Проверка Kubernetes по ТЗ

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\k8s-smoke-test.ps1
```

Этот сценарий проверяет:

- успешный rollout deployment-ов
- перезапуск pod `product-service`
- масштабирование `product-service`
- наличие `HPA`
- rolling restart `product-service`

Для локального доступа через ingress удобно добавить запись в hosts:

```text
127.0.0.1 smartthings.local
```

После этого frontend и API доступны через `http://smartthings.local`.

## CI/CD

Pipeline лежит в [ci-cd.yml](C:\Users\lehax\OneDrive\Documents\SmartThings\.github\workflows\ci-cd.yml).

Что делает pipeline:

1. Запускает backend-тесты `./gradlew test`
2. Собирает frontend `npm ci && npm run build`
3. Собирает Docker-образы всех сервисов
4. Пушит образы в Docker Hub только если тесты прошли и это push в `main` или `master`

Что нужно настроить в GitHub Secrets:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

Почему pipeline соответствует ТЗ:

- тесты запускаются всегда
- при падении тестов job сборки образов не стартует
- публикация образов не происходит при неуспешных тестах

## Конфигурация

Базовые конфиги сервисов лежат в [config-repo](C:\Users\lehax\OneDrive\Documents\SmartThings\config-repo).

Ключевые переменные окружения:

- `CONFIG_SERVER_URL`
- `EUREKA_SERVER_URL`
- `JWT_SECRET`
- `JWT_EXPIRATION_SECONDS`
- `USER_DB_URL`
- `PRODUCT_DB_URL`
- `ORDER_DB_URL`
- `NOTIFICATION_DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`

## Что можно проверить вручную

Через frontend:

- просмотр каталога
- регистрация и вход
- корзина
- оформление заказа
- просмотр своих заказов
- админский вход
- создание и редактирование товара
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

Запуск всего набора:

```powershell
.\gradlew.bat test
```

### Unit tests

Проверяют сервисный слой и бизнес-логику в изоляции.

Для чего нужны:

- быстро ловят ошибки доменных правил
- позволяют безопасно менять service-логику
- покрывают минимум по ТЗ для unit-проверок

### Integration tests

Проверяют цепочку `Controller -> Service -> Repository -> DB`.

Для чего нужны:

- подтверждают корректность JPA, web-слоя и сериализации
- ловят ошибки конфигурации контроллеров и БД

### API tests

Проверяют HTTP-статусы, JSON и обработку ошибок.

Для чего нужны:

- фиксируют контракт между backend и frontend
- не дают незаметно сломать API

### Fault tolerance tests

Проверяют circuit breaker и fallback в `order-service`.

Для чего нужны:

- подтверждают корректную деградацию при отказе зависимого сервиса
- закрывают требования ТЗ по отказоустойчивости

### Container smoke test

Запуск:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-smoke-test.ps1
```

Для чего нужен:

- подтверждает, что стенд после `docker compose up` действительно поднимается
- проверяет базовую доступность API через gateway

### Kubernetes smoke test

Запуск:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\k8s-smoke-test.ps1
```

Для чего нужен:

- проверяет pod restart
- проверяет масштабирование
- проверяет rollout поведения deployment-а

## Что проверено локально

Успешно прогнано:

- `.\gradlew.bat test`
- `npm run build`
- `docker compose config`

Дополнительно:

- `kubectl` найден в системе

Что не было прогнано в этом проходе:

- реальный деплой в активный Kubernetes-кластер
- реальный запуск GitHub Actions в удалённом репозитории

Эти два сценария зависят уже от конкретного кластера и GitHub-репозитория с настроенными secrets.
