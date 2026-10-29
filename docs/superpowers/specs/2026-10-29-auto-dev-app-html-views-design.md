# Auto Dev App HTML Views Design

## Intent

Добавить в существующий Spring Boot MVC-проект простой серверный HTML-интерфейс,
в котором человек может читать задачи агентов и их результаты, а новые задания
создавать только в отдельной scrum-таблице. Интерфейс нужен как спокойная рабочая
панель: данные должны быть легко просматриваемыми, длинные результаты — читаемыми,
а создание scrum-задачи — быстрым и однозначным.

## Goals

- Дать браузерный вход в приложение без обращения к JSON API вручную.
- Показывать основные задачи агентов в read-only таблице с фильтрами по модулю и
  статусу.
- Показывать отдельный подробный экран задачи: постановку, результат, затронутые
  классы и журнал действий.
- Показывать scrum-таблицу и разрешить через frontend только создание нового
  элемента с полями `title`, `description`, `module`.
- Сохранить существующие REST-контракты и модель данных без миграций.
- Сделать страницы адаптивными, доступными с клавиатуры и удобными для длинного
  русского текста.

## Non-goals

- Не добавлять статусы или kanban-колонки в `scrum_board_item`.
- Не давать из HTML возможность создавать, редактировать или удалять основные
  задачи, результаты, действия и затронутые классы.
- Не давать из HTML возможность редактировать или удалять scrum-элементы.
- Не добавлять аутентификацию, роли, realtime-обновления, frontend-framework или
  отдельный Node.js build.
- Не менять существующие JSON endpoints.

## Anti-goals

- Не превращать внутреннюю панель в маркетинговый интерфейс с декоративной
  анимацией, градиентами, округлыми карточками или тяжёлым JavaScript.
- Не скрывать длинные результаты в маленьких прокручиваемых областях.
- Не дублировать бизнес-правила существующих сервисов в controller или template.

## Confirmed UX and visual direction

Пользователь выбрал серверные Thymeleaf-представления с минимальным JavaScript.
Scrum остаётся существующей очередью `title / description / module`, без новых
статусов.

Палитра:

- белый и светло-серый — основной фон и области чтения;
- графитовый, тёмно-серый и чёрный — навигация, заголовки и контрастный текст;
- светло-оранжевый и рыжевато-оранжевый — активное состояние, ссылки, акцентные
  линии и кнопка создания.

Компоненты имеют прямые углы. Допустимо небольшое скругление только кнопок;
радиус остаётся малым и визуально острым. Текст кнопок имеет обычную толщину
`font-weight: 400`, но достаточный контраст и размер. Используются системные
шрифты без внешних ресурсов.

## Screens and navigation

### 1. Overview — `/ui`

- Краткие числовые показатели: всего задач, planned, in progress, completed,
  failed и количество scrum-элементов.
- Последние задачи агентов и последние scrum-элементы.
- Явные ссылки на полный список задач и scrum.

### 2. Agent tasks — `/ui/tasks`

- Read-only таблица: ID, название, модуль, статус, обновление.
- GET-фильтры `module` и `status`; фильтры не изменяют данные.
- Название ведёт на подробный экран.
- На узком экране таблица получает горизонтальную прокрутку без разрушения
  содержимого.
- Нет кнопок создания, изменения, удаления или завершения.

### 3. Task details — `/ui/tasks/{taskId}`

- Заголовок, ID, модуль, статус, даты создания и обновления.
- Отдельные полноширинные секции постановки и результата с сохранением переносов
  строк (`white-space: pre-wrap`).
- Простые read-only таблицы затронутых классов и журнала действий.
- Пустые разделы получают спокойное явное состояние, а не исчезают бесследно.
- Нет управляющих кнопок, кроме навигации назад.

### 4. Scrum — `/ui/scrum`

- Верхняя форма создания: название, модуль, подробное описание.
- Все поля обязательны; ограничения совпадают с существующим API.
- POST использует Post/Redirect/Get, чтобы обновление страницы не создавало
  дубликат.
- После успешного создания показывается flash-сообщение и новый элемент появляется
  первым.
- Ошибки валидации отображаются рядом с полями, введённые данные сохраняются.
- Ниже — таблица существующих scrum-элементов и GET-фильтр по модулю.
- Нет edit/delete controls.

Общая шапка содержит название `AUTO DEV`, ссылки «Обзор», «Задачи», «Scrum» и
индикатор только для ориентации, без меню записи в другие таблицы.

## Architecture

### MVC boundaries

Новая web-функция сохраняет существующие верхнеуровневые границы проекта:

- `controller/`: один тонкий `WebPortalController` принимает GET/POST, валидирует
  transport input, вызывает один web use case и выбирает template/redirect.
- `model/web/`: выделенные `WebPortalService`, `WebPortalRepository` и его
  реализация формируют read-модель страниц и выполняют единственную разрешённую
  mutation — создание scrum-элемента.
- `view/web/`: immutable page view models, form model и `WebPortalViewMapper`;
  Thymeleaf templates отвечают только за представление.

Так выполняются локальные требования к новому controller: он получает отдельные
service и repository. Web repository использует существующие Spring Data
repositories и не меняет их контракты. Бизнес-правила в templates и controller не
дублируются.

### Dependencies and resources

- Добавляется `spring-boot-starter-thymeleaf`.
- HTML: `src/main/resources/templates/` с общими fragments и четырьмя страницами.
- CSS: `src/main/resources/static/css/app.css`.
- JavaScript не требуется для основной функции; серверная форма и GET-фильтры
  работают без него.

### Routes

- `GET /` → redirect на `/ui`.
- `GET /ui` → overview.
- `GET /ui/tasks?module=&status=` → список задач.
- `GET /ui/tasks/{taskId}` → read-only подробности.
- `GET /ui/scrum?module=` → scrum list и форма.
- `POST /ui/scrum` → единственная HTML mutation, затем redirect.

REST endpoints под `/api/v1` остаются без изменений.

## Data flow

1. Controller получает filter или form input.
2. `WebPortalService` вызывает dedicated repository.
3. Repository читает существующие entity/repositories или сохраняет новый
   `ScrumBoardItem`.
4. Service возвращает модель use case.
5. `WebPortalViewMapper` формирует page view model.
6. Controller передаёт view model в Thymeleaf template.

Для подробностей задачи repository загружает задачу, affected classes и actions в
одном web use case. Для overview выполняются небольшие агрегаты над текущими
таблицами; объём локального приложения допускает простую реализацию без кэша.

## Validation and errors

- `ScrumItemForm`: `@NotBlank`, `@Size(max = 255)` для title/module и `@NotBlank`
  для description.
- При validation error возвращается тот же template с HTTP 200 и field errors;
  запись не выполняется.
- Неизвестная задача использует существующий `ResourceNotFoundException`; для HTML
  добавляется простой error template либо scoped MVC handler, не меняющий JSON
  формат `RestExceptionHandler` для `/api/v1`.
- HTML экранирует пользовательские строки штатными Thymeleaf expressions;
  необработанный HTML не рендерится.

## SOLID review before implementation

- SRP: controller управляет HTTP flow, service — use cases, repository — data
  access, mapper — presentation mapping, template — rendering.
- OCP: новые экраны добавляются новыми page models/templates без расширения REST
  controller условными ветками.
- LSP: repository implementation сохраняет null/filter/error semantics interface.
- ISP: web repository содержит только операции, необходимые web-порталу.
- DIP: `WebPortalService` зависит от `WebPortalRepository`, а не от JPA details.

## Testing and verification

- Controller tests через MockMvc: redirect `/`, rendering каждого GET, filters,
  отсутствие write controls, успешный POST scrum и validation failure.
- Service/repository tests: overview counts, task details aggregation, filtering и
  создание scrum-элемента.
- Mapper tests: статусы, timestamps, null result и empty collections.
- Template assertions проверяют ключевые заголовки, форму только на scrum и ссылки.
- Полная проверка `./mvnw test`.
- После сборки локальный Docker Compose backend пересобирается/перезапускается,
  затем HTTP-проверка `/ui`, `/ui/tasks`, существующей task details и `/ui/scrum`.
- Для runtime smoke создаётся отдельный тестовый scrum-элемент только если это
  необходимо; поскольку это меняет данные, предпочтительнее проверить POST в
  автоматизированном тесте и production-like UI — только GET-запросами.

## Approaches considered

1. **Server-rendered Thymeleaf pages — selected.** Лучше всего соответствует
   существующему Java MVC, работает без frontend build и позволяет жёстко отделить
   read-only screens от единственной scrum mutation.
2. **Static HTML + Fetch API.** Меньше Java-кода, но presentation flow уходит из
   MVC в JavaScript, сложнее обеспечить работу формы без JS и единое отображение
   validation errors.
3. **Single-page Thymeleaf shell.** Даёт динамические переходы, но добавляет
   состояние и сложность без пользы для маленькой внутренней панели.

## Builder decisions left open

- Точные имена Java page-model records и fragment files.
- Небольшие размеры spacing, table breakpoints и конкретные оттенки внутри
  утверждённой палитры.
- Реализация overview counts одним repository query или несколькими простыми
  count/list operations, если observable behavior остаётся тем же.
