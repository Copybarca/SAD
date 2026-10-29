# Auto Dev App

Локальный Spring Boot backend для программных агентов. Сервис хранит задачи,
результаты, журнал действий, затронутые классы и простую scrum-доску.

## Стек

- Java 21, Spring Boot 4.1.2, Spring Web MVC;
- Spring Data JPA/Hibernate без Spring Security;
- PostgreSQL 16 и Liquibase;
- Maven Wrapper, Docker и Docker Compose.

## Запуск

Для локальной Java-сборки требуется JDK 21:

```sh
./mvnw verify
```

Для полного запуска достаточно Docker Compose:

```sh
cp .env.example .env
docker compose up -d --build
curl http://localhost:8080/actuator/health
```

Значения в `.env.example` предназначены только для локальной машины. Перед
использованием в другом окружении обязательно замените пароль администратора.

При старте приложение подключается к служебной PostgreSQL database и создаёт
`${POSTGRES_APP_DB}`, если она отсутствует. Для этого административному
пользователю требуется право `CREATEDB`. Затем Liquibase применяет миграции, а
Hibernate проверяет схему с `ddl-auto=validate`.

Основные переменные:

| Переменная | Назначение | Default |
|---|---|---|
| `APP_PORT` | HTTP-порт на локальной машине | `8080` |
| `POSTGRES_HOST` | Хост PostgreSQL | `localhost` |
| `POSTGRES_PORT` | Порт PostgreSQL | `5432` |
| `POSTGRES_ADMIN_DB` | Служебная БД подключения | `postgres` |
| `POSTGRES_ADMIN_USER` | Пользователь с `CREATEDB` | `postgres` |
| `POSTGRES_ADMIN_PASSWORD` | Пароль администратора | `postgres` |
| `POSTGRES_APP_DB` | Создаваемая БД приложения | `auto_dev_app` |
| `DB_POOL_SIZE` | Максимум соединений HikariCP | `10` |

Все значения определены в `application.properties` и переопределяются
стандартным механизмом Spring Boot.

## HTML-интерфейс

После запуска откройте `http://localhost:8080/ui`.

- `/ui` — обзор состояний и последних записей;
- `/ui/tasks` — read-only список задач агентов с фильтрами;
- `/ui/tasks/{id}` — постановка, результат, затронутые классы и журнал действий;
- `/ui/scrum` — scrum-очередь и форма создания новой задачи для агента.

HTML-интерфейс не создаёт и не изменяет основные задачи, результаты, действия или
затронутые классы. Единственная операция записи из браузера — создание записи в
`scrum_board_item`; редактирование и удаление scrum-записей через HTML не доступны.

## API

Базовый prefix: `/api/v1`.

### Задачи

- `POST /tasks`
- `GET /tasks?module=planner&status=IN_PROGRESS`
- `GET /tasks/{taskId}`
- `PUT /tasks/{taskId}`
- `PATCH /tasks/{taskId}/result`
- `DELETE /tasks/{taskId}`
- `GET /tasks/{taskId}/history`

Создание задачи:

```sh
curl -X POST http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Добавить API","specification":"Реализовать историю","module":"planner"}'
```

Запись результата:

```sh
curl -X PATCH http://localhost:8080/api/v1/tasks/1/result \
  -H 'Content-Type: application/json' \
  -d '{"status":"COMPLETED","resultDescription":"API создано и проверено"}'
```

Состояния задачи: `PLANNED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`.

### Затронутые классы

- `POST /tasks/{taskId}/affected-classes`
- `GET /tasks/{taskId}/affected-classes`
- `PUT /tasks/{taskId}/affected-classes/{classId}`
- `DELETE /tasks/{taskId}/affected-classes/{classId}`

```sh
curl -X POST http://localhost:8080/api/v1/tasks/1/affected-classes \
  -H 'Content-Type: application/json' \
  -d '{"className":"ru.example.TaskService","changeDescription":"Добавлен журнал"}'
```

### Журнал действий

- `POST /tasks/{taskId}/actions`
- `GET /tasks/{taskId}/actions`
- `PUT /tasks/{taskId}/actions/{actionId}`
- `DELETE /tasks/{taskId}/actions/{actionId}`

```sh
curl -X POST http://localhost:8080/api/v1/tasks/1/actions \
  -H 'Content-Type: application/json' \
  -d '{"actionDescription":"Создана миграция","actionResult":"Таблицы готовы"}'

curl http://localhost:8080/api/v1/tasks/1/history
```

### Scrum-доска

- `POST /scrum-items`
- `GET /scrum-items?module=planner`
- `GET /scrum-items/{itemId}`
- `PUT /scrum-items/{itemId}`
- `DELETE /scrum-items/{itemId}`

```sh
curl -X POST http://localhost:8080/api/v1/scrum-items \
  -H 'Content-Type: application/json' \
  -d '{"title":"История агента","description":"Добавить API","module":"planner"}'
```

Ошибки возвращаются единообразно: `timestamp`, `status`, `error`, `message`,
`path`, `fieldErrors`.

## CI-заготовки

Скрипты находятся в `ci/`. По умолчанию `sh ci/pipeline.sh` только тестирует,
собирает JAR и вычисляет версию. Tag, image push и deploy требуют отдельных
opt-in переменных. Полный контракт описан в `ci/README.md`.

К проекту намеренно не подключены `.gitlab-ci.yml`, Telegram, SonarQube,
Kubernetes, Swarm или frontend.
