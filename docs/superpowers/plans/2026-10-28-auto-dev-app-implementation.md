# Auto Dev App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Создать локальный Spring Boot API для хранения задач агентов, их действий, результатов, затронутых классов и элементов scrum-доски с автоматическим созданием PostgreSQL БД, Liquibase, Docker Compose и безопасными CI-заготовками.

**Architecture:** Приложение построено как MVC с тонкими REST-контроллерами, отдельными model services/repositories и mapper-классами в `view`. Саму PostgreSQL database до инициализации Spring context создаёт ранний listener, структуру таблиц создаёт Liquibase, Hibernate только валидирует её. CI состоит из независимых POSIX-скриптов с единым opt-in orchestrator.

**Tech Stack:** Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA/Hibernate, Validation, Liquibase, PostgreSQL 16, Maven Wrapper, JUnit 5, Testcontainers, Docker/Compose, POSIX shell.

**Spec:** `docs/superpowers/specs/2026-10-28-auto-dev-app-design.md`

## Global Constraints

- Целевой каталог: `/home/proger/Рабочий стол/auto_dev_app`; работать в обычном checkout, worktree не создавать.
- Не выполнять commit, push, tag, registry push или deploy без отдельной команды пользователя.
- Java 21 и Spring Boot 4.1.2; зависимость Spring Security не добавлять.
- Все параметры приложения задавать в `src/main/resources/application.properties` с `${ENV_VAR:default}`.
- Liquibase — единственный владелец DDL; `spring.jpa.hibernate.ddl-auto=validate`.
- Пакеты верхнего уровня новой функциональности обязаны разделять `model/`, `view/`, `controller/`.
- DTO располагаются в `controller/dto`; JPA-сущности наружу не возвращаются.
- Каждый REST-контроллер имеет отдельный service и repository своего use case; контроллеры не содержат бизнес-логику.
- Над каждым production-классом разместить краткий русский Javadoc о назначении; над методами комментарии не писать.
- CI-заготовки не подключать к Compose, GitLab CI, registry или deploy; опасные действия только через явные opt-in переменные.
- Frontend, Security, Kubernetes, Swarm, Telegram, SonarQube и SAST не добавлять.

## Review Focus

- Пустые или состоящие из пробелов обязательные строки должны давать `400`, не доходя до persistence — Task 4 и Task 6.
- Дочерний `classId`/`actionId` от другой задачи должен давать `404`, а не изменять чужую запись — Task 5.
- Повторный bootstrap существующей БД не должен выполнять `CREATE DATABASE` — Task 3.
- Некорректное имя БД должно отклоняться до SQL, исключая SQL injection через identifier — Task 3.
- CI orchestrator не должен выполнять tag/push/deploy без явных opt-in переменных — Task 8.

---

### Task 1: Воспроизводимый каркас Spring Boot

**Files:**
- Create: `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`
- Create: `.gitignore`, `.dockerignore`
- Create: `src/test/java/ru/autodevapp/AutoDevAppApplicationTest.java`
- Create: `src/main/java/ru/autodevapp/AutoDevAppApplication.java`
- Create: `src/main/resources/application.properties`
- Create: `src/main/java/ru/autodevapp/model/package-info.java`
- Create: `src/main/java/ru/autodevapp/view/package-info.java`
- Create: `src/main/java/ru/autodevapp/controller/package-info.java`

**Interfaces:**
- Consumes: официальный Spring Initializr с параметрами `bootVersion=4.1.2`, `javaVersion=21`, Maven, package `ru.autodevapp`.
- Produces: executable Spring Boot application и Maven lifecycle; application properties для PostgreSQL/Liquibase/Actuator.

- [ ] **Step 1: Получить только build scaffolding**

Сгенерировать Maven Wrapper и `pom.xml` через Spring Initializr с зависимостями `web`, `data-jpa`, `validation`, `liquibase`, `postgresql`, `actuator`, `testcontainers`; удалить сгенерированный production-класс, если он присутствует. В `pom.xml` добавить test dependencies `org.testcontainers:postgresql` и `org.springframework.boot:spring-boot-testcontainers`.

- [ ] **Step 2: Написать падающий smoke-test**

`AutoDevAppApplicationTest.applicationClassIsSpringBootEntryPoint()` проверяет наличие `@SpringBootApplication` на `AutoDevAppApplication`; compilation должна падать из-за отсутствующего класса.

- [ ] **Step 3: Проверить RED**

Run: `./mvnw -Dtest=AutoDevAppApplicationTest test`

Expected: FAIL compilation — `AutoDevAppApplication` не найден.

- [ ] **Step 4: Добавить минимальную точку входа и конфигурацию**

Создать `AutoDevAppApplication` с `main`, три MVC `package-info.java`, `.gitignore`, `.dockerignore` и `application.properties` со свойствами `server.port`, `spring.datasource.*`, `spring.liquibase.*`, `spring.jpa.*`, `management.endpoints.web.exposure.include=health,info`, `management.endpoint.health.probes.enabled=true`, `app.database.admin-url`, `app.database.admin-username`, `app.database.admin-password`, `app.database.name`.

- [ ] **Step 5: Проверить GREEN**

Run: `./mvnw -Dtest=AutoDevAppApplicationTest test`

Expected: PASS.

- [ ] **Step 6: Зафиксировать checkpoint без commit**

Run: `git status --short` после `git init`; убедиться, что нет сгенерированных секретов, IDE-файлов и build output.

---

### Task 2: Liquibase-схема, сущности и repositories

**Files:**
- Create: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Create: `src/main/resources/db/changelog/changes/001-create-agent-tables.yaml`
- Create: `src/main/java/ru/autodevapp/model/entity/{DevelopmentTask,AffectedClass,TaskAction,ScrumBoardItem,TaskStatus}.java`
- Create: `src/main/java/ru/autodevapp/model/repository/{DevelopmentTaskRepository,AffectedClassRepository,TaskActionRepository,ScrumBoardItemRepository}.java`
- Create: `src/test/java/ru/autodevapp/model/repository/DatabaseSchemaIntegrationTest.java`
- Create: `src/test/java/ru/autodevapp/support/PostgresContainerSupport.java`

**Interfaces:**
- Produces: JPA entities; repository methods `findAllByOrderByCreatedAtDescIdDesc`, task filters, `findByIdAndTaskId`, `findAllByTaskIdOrderByCreatedAtAscIdAsc`; Liquibase schema matching all mappings.

- [ ] **Step 1: Написать падающий Testcontainers schema-test**

`DatabaseSchemaIntegrationTest` стартует PostgreSQL 16, запускает Liquibase/JPA context и проверяет: все четыре repository сохраняют/читают сущности; `(task_id,class_name)` уникален; удаление задачи каскадно удаляет children; порядок history стабилен.

- [ ] **Step 2: Проверить RED**

Run: `./mvnw -Dtest=DatabaseSchemaIntegrationTest test`

Expected: FAIL — отсутствуют changelog, entity и repository.

- [ ] **Step 3: Создать начальную Liquibase migration**

Определить `development_task`, `affected_class`, `task_action`, `scrum_board_item`, identity PK, UTC timestamps, enum-compatible `VARCHAR(32)`, FK с `ON DELETE CASCADE`, unique `(task_id,class_name)` и индексы из спецификации.

- [ ] **Step 4: Реализовать JPA model и repository interfaces**

Сущности используют `Instant`, lifecycle callbacks для `createdAt/updatedAt`, `EnumType.STRING`, lazy child relationships и package-level repository query methods. Не добавлять Lombok.

- [ ] **Step 5: Проверить GREEN**

Run: `./mvnw -Dtest=DatabaseSchemaIntegrationTest test`

Expected: PASS, Liquibase применяет changeset, Hibernate validate проходит.

- [ ] **Step 6: Запустить весь текущий suite**

Run: `./mvnw test`

Expected: PASS.

---

### Task 3: Идемпотентное создание PostgreSQL database

**Files:**
- Create: `src/main/java/ru/autodevapp/model/database/{DatabaseSettings,AdminConnectionFactory,DriverManagerAdminConnectionFactory,DatabaseProvisioner,DatabaseProvisioningListener}.java`
- Modify: `src/main/java/ru/autodevapp/AutoDevAppApplication.java`
- Create: `src/test/java/ru/autodevapp/model/database/{DatabaseSettingsTest,DatabaseProvisionerIntegrationTest,DatabaseProvisioningListenerTest}.java`

**Interfaces:**
- `DatabaseSettings.from(Environment): DatabaseSettings`
- `AdminConnectionFactory.open(DatabaseSettings): Connection`
- `DatabaseProvisioner.provision(DatabaseSettings): void`
- `DatabaseProvisioningListener.onApplicationEvent(ApplicationEnvironmentPreparedEvent): void`

- [ ] **Step 1: Написать падающие tests настроек и имени БД**

Проверить обязательность admin URL/user/password/name и допустимые имена по regex `[A-Za-z_][A-Za-z0-9_]{0,62}`; пустые, hyphenated и SQL-like names отклоняются понятным `IllegalArgumentException` до JDBC.

- [ ] **Step 2: Проверить RED настроек**

Run: `./mvnw -Dtest=DatabaseSettingsTest test`

Expected: FAIL compilation — классы отсутствуют.

- [ ] **Step 3: Реализовать immutable settings и JDBC factory**

Читать только четыре `app.database.*` property, не логировать password, connection создавать через `DriverManager`.

- [ ] **Step 4: Написать падающий Testcontainers provisioning test**

Проверить создание отсутствующей БД, no-op при повторном вызове, отказ ограниченному пользователю без `CREATEDB`, отсутствие SQL-инъекции через имя.

- [ ] **Step 5: Проверить RED provisioner**

Run: `./mvnw -Dtest=DatabaseProvisionerIntegrationTest test`

Expected: FAIL — provisioner отсутствует/не создаёт БД.

- [ ] **Step 6: Реализовать provisioner**

Через prepared query `SELECT 1 FROM pg_database WHERE datname = ?` проверить существование; `CREATE DATABASE` выполнять только для заранее провалидированного identifier, заключённого в двойные кавычки. JDBC ошибки оборачивать в startup exception без credentials.

- [ ] **Step 7: Написать и провести RED/GREEN listener-test**

Проверить, что listener извлекает environment и вызывает provisioner ровно до context refresh; зарегистрировать listener через `SpringApplication.addListeners(...)` в `main`.

- [ ] **Step 8: Проверить task suite**

Run: `./mvnw -Dtest='DatabaseSettingsTest,DatabaseProvisionerIntegrationTest,DatabaseProvisioningListenerTest' test`

Expected: PASS.

---

### Task 4: API основных задач и единые ошибки

**Files:**
- Create: `src/main/java/ru/autodevapp/controller/dto/task/{CreateTaskRequest,UpdateTaskRequest,UpdateTaskResultRequest,TaskResponse,TaskDetailsResponse}.java`
- Create: `src/main/java/ru/autodevapp/model/service/DevelopmentTaskService.java`
- Create: `src/main/java/ru/autodevapp/view/DevelopmentTaskViewMapper.java`
- Create: `src/main/java/ru/autodevapp/controller/DevelopmentTaskController.java`
- Create: `src/main/java/ru/autodevapp/exception/{ResourceNotFoundException,DuplicateResourceException,ApiErrorResponse,RestExceptionHandler}.java`
- Create: `src/test/java/ru/autodevapp/model/service/DevelopmentTaskServiceTest.java`
- Create: `src/test/java/ru/autodevapp/view/DevelopmentTaskViewMapperTest.java`
- Create: `src/test/java/ru/autodevapp/controller/DevelopmentTaskControllerTest.java`

**Interfaces:**
- `DevelopmentTaskService.create/update/updateResult/findById/findAll/delete` принимает model primitives/entity values и возвращает entities.
- `DevelopmentTaskViewMapper.toResponse(DevelopmentTask)` и `toDetailsResponse(DevelopmentTask)` возвращают DTO.
- Routes: `POST/GET /api/v1/tasks`, `GET/PUT/DELETE /api/v1/tasks/{taskId}`, `PATCH /api/v1/tasks/{taskId}/result`.

- [ ] **Step 1: Написать service tests до реализации**

Проверить defaults `PLANNED`, фильтры module/status, сохранение result/status, `ResourceNotFoundException`, физическое delete. Использовать in-memory fake repository adapter либо Mockito только на repository boundary; утверждать состояние/возврат service, не mock interactions кроме delete boundary.

- [ ] **Step 2: Проверить RED service**

Run: `./mvnw -Dtest=DevelopmentTaskServiceTest test`

Expected: FAIL compilation.

- [ ] **Step 3: Реализовать service и mapper**

Service пометить `@Transactional`, read methods `readOnly=true`; mapper не содержит бизнес-решений и возвращает immutable records.

- [ ] **Step 4: Написать падающие MVC contract tests**

Проверить `201/200/204/400/404`, `Location` для POST, JSON field names, фильтры, blank title/specification/module, неизвестный enum, единый error body и отсутствие stack trace.

- [ ] **Step 5: Проверить RED MVC**

Run: `./mvnw -Dtest=DevelopmentTaskControllerTest test`

Expected: FAIL — controller/DTO/handler отсутствуют.

- [ ] **Step 6: Реализовать DTO, controller и exception handler**

Controller выполняет только validation, service call, mapping и HTTP response. Ограничения: title/module 1..255, specification nonblank text, result nonblank при `COMPLETED`/`FAILED` проверяется service.

- [ ] **Step 7: Проверить GREEN и regression**

Run: `./mvnw -Dtest='DevelopmentTaskServiceTest,DevelopmentTaskViewMapperTest,DevelopmentTaskControllerTest' test && ./mvnw test`

Expected: PASS.

---

### Task 5: API затронутых классов, действий и агрегированной истории

**Files:**
- Create: `src/main/java/ru/autodevapp/controller/dto/affectedclass/{AffectedClassRequest,AffectedClassResponse}.java`
- Create: `src/main/java/ru/autodevapp/controller/dto/action/{TaskActionRequest,TaskActionResponse}.java`
- Create: `src/main/java/ru/autodevapp/controller/dto/history/TaskHistoryResponse.java`
- Create: `src/main/java/ru/autodevapp/model/service/{AffectedClassService,TaskActionService}.java`
- Create: `src/main/java/ru/autodevapp/view/{AffectedClassViewMapper,TaskActionViewMapper,TaskHistoryViewMapper}.java`
- Create: `src/main/java/ru/autodevapp/controller/{AffectedClassController,TaskActionController}.java`
- Modify: `src/main/java/ru/autodevapp/model/service/DevelopmentTaskService.java`
- Modify: `src/main/java/ru/autodevapp/controller/DevelopmentTaskController.java`
- Test: matching `model/service`, `view`, and `controller` test classes.

**Interfaces:**
- `AffectedClassService.create/list/update/delete(taskId, classId, ...)`
- `TaskActionService.create/list/update/delete(taskId, actionId, ...)`
- `DevelopmentTaskService.getHistory(taskId): DevelopmentTask`
- Routes exactly as the approved spec under `/api/v1/tasks/{taskId}/affected-classes`, `/actions`, `/history`.

- [ ] **Step 1: Провести RED/GREEN для AffectedClassService**

Tests проверяют create/list/update/delete, duplicate class -> `409`, отсутствующую task -> `404`, class from another task -> `404`. Затем реализовать отдельный service поверх `AffectedClassRepository` и `DevelopmentTaskRepository`.

- [ ] **Step 2: Провести RED/GREEN для AffectedClassController**

MVC tests проверяют routes, validation `className` 1..500 и nonblank `changeDescription`, JSON и response codes; реализовать DTO, mapper и thin controller.

- [ ] **Step 3: Провести RED/GREEN для TaskActionService**

Tests проверяют stable chronological ordering, create/update/delete, отсутствующую task и action from another task -> `404`; реализовать отдельный service.

- [ ] **Step 4: Провести RED/GREEN для TaskActionController**

MVC tests проверяют routes, nonblank `actionDescription`, nullable `actionResult`, JSON и response codes; реализовать DTO, mapper и thin controller.

- [ ] **Step 5: Провести RED/GREEN агрегированной истории**

Test `GET /api/v1/tasks/{taskId}/history` утверждает основные поля, итог, affected classes и actions в хронологическом порядке одним response. Реализовать `TaskHistoryResponse`, mapper и метод существующего `DevelopmentTaskController`, не создавая отдельный controller.

- [ ] **Step 6: Проверить regression**

Run: `./mvnw test`

Expected: PASS.

---

### Task 6: CRUD API scrum-доски

**Files:**
- Create: `src/main/java/ru/autodevapp/controller/dto/scrum/{ScrumBoardItemRequest,ScrumBoardItemResponse}.java`
- Create: `src/main/java/ru/autodevapp/model/service/ScrumBoardItemService.java`
- Create: `src/main/java/ru/autodevapp/view/ScrumBoardItemViewMapper.java`
- Create: `src/main/java/ru/autodevapp/controller/ScrumBoardItemController.java`
- Create: corresponding service/mapper/controller tests.

**Interfaces:**
- `ScrumBoardItemService.create/findAll/findById/update/delete`
- Routes: `POST/GET /api/v1/scrum-items`, `GET/PUT/DELETE /api/v1/scrum-items/{itemId}`; optional `module` filter.

- [ ] **Step 1: Написать и проверить RED service tests**

Проверить CRUD, newest-first order, module filter, not found.

- [ ] **Step 2: Реализовать minimal service и mapper; проверить GREEN**

Run: `./mvnw -Dtest='ScrumBoardItemServiceTest,ScrumBoardItemViewMapperTest' test`

Expected: PASS.

- [ ] **Step 3: Написать и проверить RED MVC tests**

Проверить `201/200/204/400/404`, blank title/description/module и JSON contract.

- [ ] **Step 4: Реализовать DTO и thin controller; проверить GREEN**

Run: `./mvnw -Dtest=ScrumBoardItemControllerTest test && ./mvnw test`

Expected: PASS.

---

### Task 7: Dockerfile и локальный Compose

**Files:**
- Create: `Dockerfile`
- Create: `compose.yaml`
- Create: `.env.example`
- Create: `src/test/java/ru/autodevapp/config/ApplicationPropertiesTest.java`

**Interfaces:**
- Compose services: `postgres` (PostgreSQL 16 Alpine) и `app`.
- Environment contract: `APP_PORT`, `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_ADMIN_DB`, `POSTGRES_ADMIN_USER`, `POSTGRES_ADMIN_PASSWORD`, `POSTGRES_APP_DB`, matching Spring datasource/admin variables.

- [ ] **Step 1: Написать падающий property binding test**

Проверить, что test environment может задать все datasource/admin/port значения standard environment variables и Spring разрешает placeholders.

- [ ] **Step 2: Проверить RED, затем привести properties к contract**

Run: `./mvnw -Dtest=ApplicationPropertiesTest test`

Expected after implementation: PASS.

- [ ] **Step 3: Создать multi-stage Alpine Dockerfile**

Builder `eclipse-temurin:21-jdk-alpine`, runtime `eclipse-temurin:21-jre-alpine`; non-root user, fat JAR, exposed port, BusyBox-compatible healthcheck `/actuator/health`.

- [ ] **Step 4: Создать Compose без Swarm/deploy sections**

PostgreSQL healthcheck, named volume, app `depends_on` healthy DB, все переменные явно перечислены, defaults только для локальной разработки.

- [ ] **Step 5: Валидировать конфигурацию**

Run: `docker compose --env-file .env.example config`

Expected: exit 0, оба services и volume присутствуют, secrets не напечатаны из реальных файлов.

- [ ] **Step 6: Собрать image**

Run: `docker build -t auto-dev-app:local .`

Expected: exit 0; image использует Java 21 и non-root user.

---

### Task 8: Функциональные CI-заготовки

**Files:**
- Create: `ci/{common,test,build-artifact,version,auto-tag,build-image,push-image,deploy,pipeline}.sh`
- Create: `ci/tests/{version-test,pipeline-safe-defaults-test}.sh`
- Create: `ci/README.md`

**Interfaces:**
- Every script: POSIX `sh`, `set -eu`, callable from project root or any working directory.
- `version.sh` writes `VERSION`, `DOCKER_IMAGE_TAG`, `DOCKER_IMAGE` to `${CI_ENV_FILE:-build.env}`.
- Opt-ins: `CI_CREATE_TAG`, `CI_PUSH_TAG`, `CI_BUILD_IMAGE`, `CI_PUSH_IMAGE`, `CI_EXECUTE_DEPLOY`; all default `false`.
- `pipeline.sh`: test -> artifact -> version, then optional tag/image/push/deploy.

- [ ] **Step 1: Написать падающие version rules tests**

В temporary git repositories проверить: no tags -> `1.0.0`; `feature!/*` -> major; `feature/*` -> minor; fix/hotfix/perf/refactor/revert -> patch; doc/docs/test/chore/build/ci -> no new tag; merge-title fallback; existing tag on HEAD -> no duplicate.

- [ ] **Step 2: Проверить RED**

Run: `sh ci/tests/version-test.sh`

Expected: FAIL — scripts отсутствуют.

- [ ] **Step 3: Реализовать common/version/auto-tag**

Реализовать переносимый алгоритм SemVer без CI-specific network actions. Tag создаётся только при `CI_CREATE_TAG=true`, push только при дополнительном `CI_PUSH_TAG=true`.

- [ ] **Step 4: Проверить GREEN version rules**

Run: `sh ci/tests/version-test.sh`

Expected: PASS.

- [ ] **Step 5: Написать падающий safe-defaults orchestrator test**

Подменить executables test doubles и доказать, что default pipeline вызывает test/artifact/version, но не tag, Docker login/build/push и deploy command.

- [ ] **Step 6: Реализовать остальные stage scripts и orchestrator**

`test.sh` запускает `./mvnw verify`; `build-artifact.sh` — `./mvnw -DskipTests package`; Docker scripts требуют repository/credentials; `deploy.sh` только описывает будущий hook и исполняет `DEPLOY_COMMAND` при `CI_EXECUTE_DEPLOY=true`. Никакой связи с Compose.

- [ ] **Step 7: Проверить shell suite и syntax**

Run: `for script in ci/*.sh ci/tests/*.sh; do sh -n "$script"; done && sh ci/tests/version-test.sh && sh ci/tests/pipeline-safe-defaults-test.sh`

Expected: PASS; ни tag, ни push, ни deploy не выполнены.

- [ ] **Step 8: Документировать переменные CI**

`ci/README.md` объясняет каждый скрипт, порядок, inputs/outputs и будущий вызов из GitLab jobs без добавления `.gitlab-ci.yml`.

---

### Task 9: Пользовательская документация и сквозная проверка

**Files:**
- Create: `README.md`
- Modify: production classes only if SOLID/MVC review finds a scoped violation.

**Interfaces:**
- README covers local prerequisites, `./mvnw verify`, `docker compose up -d --build`, environment table, DB bootstrap, migrations, API routes and curl examples.

- [ ] **Step 1: Написать README по фактическому интерфейсу**

Добавить curl examples для task creation/result, affected class, action/history, scrum item; явно указать отсутствие Security и локальный характер default credentials.

- [ ] **Step 2: Выполнить SOLID/MVC self-review**

Проверить `model/view/controller`, thin controllers, inward dependencies, отдельные service/repository, DTO/entity separation, отсутствие business rules в mapper и отсутствие незапрошенных contract changes. Исправления проводить через новый failing test.

- [ ] **Step 3: Проверить комментарии и запрещённые зависимости**

Run: `grep -R "spring-boot-starter-security" pom.xml && exit 1 || true`; просмотреть все production classes: русский class-level Javadoc есть, method-level boilerplate comments отсутствуют.

- [ ] **Step 4: Полная Maven verification**

Run: `./mvnw verify`

Expected: BUILD SUCCESS; все unit/MVC/integration tests pass или Testcontainers tests явно skipped только при недоступном Docker.

- [ ] **Step 5: Полная shell verification**

Run: `for script in ci/*.sh ci/tests/*.sh; do sh -n "$script"; done && sh ci/tests/version-test.sh && sh ci/tests/pipeline-safe-defaults-test.sh`

Expected: PASS.

- [ ] **Step 6: Compose smoke-test без deploy**

Run: `docker compose --env-file .env.example up -d --build`, дождаться `GET http://localhost:${APP_PORT}/actuator/health` = `UP`, выполнить минимальный create/read history API scenario, затем `docker compose --env-file .env.example down` без `-v`, чтобы данные оставались recoverable.

- [ ] **Step 7: Проверить итоговый diff**

Run: `git status --short && git diff --check && git diff --stat`

Expected: только файлы `auto_dev_app`, нет credentials, build output, IDE metadata, commits, tags или внешних изменений.
