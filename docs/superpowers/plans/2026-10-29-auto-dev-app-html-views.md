# Auto Dev App HTML Views Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить в auto_dev_app серверный HTML-интерфейс для read-only просмотра задач агентов и создания элементов только в существующей scrum-таблице.

**Architecture:** Новый тонкий `WebPortalController` использует dedicated `WebPortalService` и `WebPortalRepository`; mapper преобразует domain entities в immutable page view models, а Thymeleaf отвечает только за HTML. Существующие `/api/v1` контракты и схема БД сохраняются, единственная HTML mutation — `POST /ui/scrum`.

**Tech Stack:** Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, Thymeleaf, Jakarta Validation, JUnit 5, MockMvc, Mockito, AssertJ.

**Spec:** `docs/superpowers/specs/2026-10-29-auto-dev-app-html-views-design.md`

## Global Constraints

- Основные задачи, результаты, affected classes и actions доступны из HTML только для чтения.
- Scrum сохраняет поля `title`, `description`, `module`; статусы и schema migration не добавляются.
- Единственная HTML mutation — создание scrum-элемента.
- REST endpoints `/api/v1` и их JSON error format не изменяются.
- Палитра: белый/серый/графитовый/чёрный с оранжевыми акцентами; углы прямые, кнопки с малым радиусом и `font-weight: 400`.
- Новый Java web feature сохраняет явные MVC-границы и dedicated controller/service/repository.
- Не выполнять commit, push или deployment без отдельной команды пользователя.

## Review Focus

- Пустой `resultDescription` должен отображаться как явное read-only состояние, а не ломать task details.
- Неизвестный task ID должен вернуть HTML 404, не JSON и не HTTP 500.
- Blank/слишком длинные scrum fields должны вернуть форму с field errors и не вызвать repository save.
- Module/status filters должны быть GET-only, сочетаться между собой и сохраняться в форме.
- HTML не должен содержать POST/PUT/PATCH/DELETE controls для development tasks, actions или affected classes.

---

### Task 1: Dedicated web model, repository and service

**Files:**
- Create: `src/main/java/ru/autodevapp/model/web/WebDashboardData.java`
- Create: `src/main/java/ru/autodevapp/model/web/repository/WebPortalRepository.java`
- Create: `src/main/java/ru/autodevapp/model/web/repository/JpaWebPortalRepository.java`
- Create: `src/main/java/ru/autodevapp/model/web/service/WebPortalService.java`
- Test: `src/test/java/ru/autodevapp/model/web/service/WebPortalServiceTest.java`
- Test: `src/test/java/ru/autodevapp/model/web/repository/JpaWebPortalRepositoryTest.java`

**Interfaces:**
- Produces: `WebPortalService.loadDashboard()`, `findTasks(String, TaskStatus)`, `findTask(Long)`, `findScrumItems(String)`, `createScrumItem(String, String, String)`.
- Produces: `WebDashboardData(long totalTasks, Map<TaskStatus, Long> countsByStatus, List<DevelopmentTask> recentTasks, long scrumCount, List<ScrumBoardItem> recentScrumItems)`.

- [ ] **Step 1: Write failing service tests** for status counts, five-item recent limits, combined filters, missing task, and scrum creation delegation.
- [ ] **Step 2: Run** `./mvnw -Dtest=WebPortalServiceTest test`; expect compilation/test failure because web service types do not exist.
- [ ] **Step 3: Implement web domain record, repository interface and service** with constructor injection, read-only transactions and `ResourceNotFoundException("Задача " + id + " не найдена")`.
- [ ] **Step 4: Run** `./mvnw -Dtest=WebPortalServiceTest test`; expect PASS.
- [ ] **Step 5: Write failing repository tests** for four task-filter branches, detailed entity graph lookup, scrum module filter and save.
- [ ] **Step 6: Implement `JpaWebPortalRepository`** by composing existing Spring Data repositories; do not duplicate SQL or mutate existing REST services.
- [ ] **Step 7: Run** `./mvnw -Dtest=JpaWebPortalRepositoryTest,WebPortalServiceTest test`; expect PASS.

### Task 2: Immutable page view models and mapper

**Files:**
- Create: `src/main/java/ru/autodevapp/view/web/DashboardPageView.java`
- Create: `src/main/java/ru/autodevapp/view/web/TaskListPageView.java`
- Create: `src/main/java/ru/autodevapp/view/web/TaskDetailsPageView.java`
- Create: `src/main/java/ru/autodevapp/view/web/ScrumPageView.java`
- Create: `src/main/java/ru/autodevapp/view/web/ScrumItemForm.java`
- Create: `src/main/java/ru/autodevapp/view/web/WebPortalViewMapper.java`
- Test: `src/test/java/ru/autodevapp/view/web/WebPortalViewMapperTest.java`

**Interfaces:**
- Consumes: Task 1 domain entities and `WebDashboardData`.
- Produces: `toDashboardPage`, `toTaskListPage`, `toTaskDetailsPage`, `toScrumPage` and validated `ScrumItemForm`.

- [ ] **Step 1: Write failing mapper tests** for status labels/CSS keys, UTC timestamp formatting, null result fallback, empty child collections, selected filters and distinct sorted module options.
- [ ] **Step 2: Run** `./mvnw -Dtest=WebPortalViewMapperTest test`; expect failure because page models/mapper do not exist.
- [ ] **Step 3: Implement focused records and mapper**; keep all HTML out of Java strings and expose only values templates need.
- [ ] **Step 4: Run** `./mvnw -Dtest=WebPortalViewMapperTest test`; expect PASS.

### Task 3: MVC routes, validation and HTML-specific errors

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/ru/autodevapp/controller/WebPortalController.java`
- Test: `src/test/java/ru/autodevapp/controller/WebPortalControllerTest.java`

**Interfaces:**
- Consumes: `WebPortalService`, `WebPortalViewMapper`, `ScrumItemForm`.
- Produces: `GET /`, `GET /ui`, `GET /ui/tasks`, `GET /ui/tasks/{taskId}`, `GET /ui/scrum`, `POST /ui/scrum`.

- [ ] **Step 1: Add Thymeleaf dependency and write failing MockMvc tests** for all routes, combined filters, HTML 404, valid Post/Redirect/Get, flash success and invalid form without service mutation.
- [ ] **Step 2: Run** `./mvnw -Dtest=WebPortalControllerTest test`; expect failure because controller/templates do not exist.
- [ ] **Step 3: Implement thin controller** with `@Controller`, `@Valid @ModelAttribute`, `BindingResult`, redirect attributes and a controller-scoped `@ExceptionHandler(ResourceNotFoundException.class)` returning `error/not-found` with HTTP 404.
- [ ] **Step 4: Run** `./mvnw -Dtest=WebPortalControllerTest test`; template resolution may remain failing until Task 4, while route/model assertions must compile.

### Task 4: Thymeleaf views and visual system

**Files:**
- Create: `src/main/resources/templates/fragments/layout.html`
- Create: `src/main/resources/templates/dashboard.html`
- Create: `src/main/resources/templates/tasks/list.html`
- Create: `src/main/resources/templates/tasks/details.html`
- Create: `src/main/resources/templates/scrum/index.html`
- Create: `src/main/resources/templates/error/not-found.html`
- Create: `src/main/resources/static/css/app.css`
- Test: `src/test/java/ru/autodevapp/controller/WebPortalTemplateTest.java`

**Interfaces:**
- Consumes: Task 2 page models and Task 3 route model attributes.
- Produces: responsive, accessible HTML with no development-task write controls.

- [ ] **Step 1: Write failing rendered-template tests** asserting shared navigation, task table/detail content, pre-wrapped result, scrum form, validation messages, and absence of forbidden task mutation forms.
- [ ] **Step 2: Run** `./mvnw -Dtest=WebPortalControllerTest,WebPortalTemplateTest test`; expect missing-template failures.
- [ ] **Step 3: Implement layout and four screens** with semantic headings, labels, tables, empty states and Post/Redirect/Get flash message.
- [ ] **Step 4: Implement CSS** with the approved palette, square components, small-radius regular-weight buttons, responsive table wrappers, visible focus and readable long-form sections.
- [ ] **Step 5: Run** `./mvnw -Dtest=WebPortalControllerTest,WebPortalTemplateTest test`; expect PASS.

### Task 5: Full verification and local runtime

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: completed HTML feature.
- Produces: documented routes and verified local application.

- [ ] **Step 1: Update README** with `/ui` entry point, read-only boundaries and scrum-only write behavior.
- [ ] **Step 2: Run focused tests** for Tasks 1–4; expect zero failures.
- [ ] **Step 3: Run full suite** `./mvnw test`; expect BUILD SUCCESS and zero test failures.
- [ ] **Step 4: Run SOLID/MVC self-review**: verify explicit controller/model/view packages, thin controller, dedicated service/repository, inward dependencies and no business rules in templates.
- [ ] **Step 5: Rebuild/restart local app** with `docker compose up -d --build app`; do not remove volumes or recreate PostgreSQL data.
- [ ] **Step 6: Verify over HTTP**: `/ui`, `/ui/tasks`, `/ui/tasks/6`, `/ui/scrum`, CSS asset and existing `/api/v1/tasks/6` all return expected 200/content; unknown task returns HTML 404.
- [ ] **Step 7: Inspect `git diff` and `git status`** to confirm only intended files changed and all pre-existing untracked files remain preserved.

