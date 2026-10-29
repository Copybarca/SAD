# CI-заготовки

Каталог содержит независимые POSIX-скрипты для CI-задач проекта.

## Автоматическое тегирование в GitHub

Workflow `.github/workflows/auto-tag.yml` запускается после push в `develop`,
`main` или `master`. Для merge-коммита он получает исходную ветку pull request
через GitHub API и применяет правила `ci/version.sh`. Прямой push в одну из
целевых веток повышает patch-версию. Созданный тег отправляется в `origin`
скриптом `ci/auto-tag.sh` через встроенный `GITHUB_TOKEN`.

Workflow выполняет только расчёт версии и создание тега. Он не запускает
Java, Maven, тесты, сборку Docker-образа или деплой. Для работы необходимы
разрешения `contents: write` и `pull-requests: read`, заданные непосредственно
в workflow.

`pipeline.sh` по умолчанию выполняет только тесты, сборку JAR и вычисление
версии. Остальные этапы требуют явного включения:

- `CI_CREATE_TAG=true` — создать локальный SemVer-тег;
- `CI_PUSH_TAG=true` — отправить созданный тег в `origin`;
- `CI_BUILD_IMAGE=true` и `DOCKER_IMAGE_REPO` — собрать Docker image;
- `CI_PUSH_IMAGE=true` плюс `CI_REGISTRY`, `CI_REGISTRY_USER`,
  `CI_REGISTRY_PASSWORD` — отправить image;
- `CI_EXECUTE_DEPLOY=true` и `DEPLOY_COMMAND` — выполнить внешний deploy hook.

`version.sh` записывает `VERSION`, `SHOULD_CREATE_TAG`, `DOCKER_IMAGE_TAG` и
опциональный `DOCKER_IMAGE` в `${CI_ENV_FILE:-build.env}`. Правила веток
следующие: `feature!` — major, `feature` — minor, исправления — patch,
служебные ветки не создают тег, первая версия — `1.0.0`.

Отдельная GitLab job при необходимости может вызывать каждый скрипт по stage
или `sh ci/pipeline.sh`. Секреты передаются только через protected variables.
