# GitHub Auto Tag Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Создавать SemVer-тег после merge в основные ветки и после прямого push в них, не запуская сборку приложения.

**Architecture:** Единственный GitHub Actions workflow запускается на push в `develop`, `main` и `master`. Он определяет исходную ветку связанного merged pull request через GitHub API, затем вызывает существующие `ci/version.sh` и `ci/auto-tag.sh`.

**Tech Stack:** GitHub Actions, POSIX shell, GitHub CLI, Git.

**Spec:** `docs/superpowers/specs/2026-10-29-github-auto-tag-design.md`

## Global Constraints

- Не запускать Java, Maven, тестовые jobs, Docker или деплой.
- Не дублировать правила SemVer из `ci/version.sh` в workflow.
- Запускать workflow только для push в `develop`, `main` и `master`.
- Прямой push повышает patch; merge использует префикс исходной ветки.
- Не создавать и не отправлять реальные теги во время локальной проверки.
- Не коммитить и не отправлять изменения без отдельного указания пользователя.

## Review Focus

- Merge из `feature/*` должен передать исходную ветку в `version.sh`, а не имя целевой ветки.
- Прямой push без связанного merged pull request должен использовать `develop`, `main` или `master` и повысить patch.
- Merge из служебной ветки должен завершиться без создания тега.
- Повторный запуск на уже помеченном SHA не должен создавать новый тег.
- Checkout должен получить всю историю тегов, иначе версия может ошибочно начаться с `1.0.0`.

---

### Task 1: GitHub workflow автотегирования

**Files:**
- Create: `.github/workflows/auto-tag.yml`
- Modify: `ci/README.md`
- Verify: `ci/tests/version-test.sh`

**Interfaces:**
- Consumes: `GITHUB_SHA`, `GITHUB_REF_NAME`, `GITHUB_REPOSITORY`, `GITHUB_TOKEN`; `ci/version.sh`; `ci/auto-tag.sh`.
- Produces: pushed lightweight SemVer tag on the current commit, or a successful no-op when the branch rules suppress tagging.

- [ ] **Step 1: Add `.github/workflows/auto-tag.yml`**

  Configure `push.branches` for `develop`, `main`, and `master`; set
  `contents: write` and `pull-requests: read`; checkout with `fetch-depth: 0`.

- [ ] **Step 2: Resolve the source branch**

  Query `repos/$GITHUB_REPOSITORY/commits/$GITHUB_SHA/pulls`, select a merged
  pull request whose base is `GITHUB_REF_NAME`, and write its `head.ref` to a
  step output. Fall back to `GITHUB_REF_NAME` for a direct push.

- [ ] **Step 3: Invoke the existing version and tag scripts**

  Pass the resolved branch as `CI_SOURCE_BRANCH`, run `sh ci/version.sh`, set
  the GitHub Actions bot identity, then run `ci/auto-tag.sh` with
  `CI_CREATE_TAG=true` and `CI_PUSH_TAG=true`.

- [ ] **Step 4: Document the connected workflow**

  Update `ci/README.md` to name the GitHub workflow, its trigger, permissions,
  direct-push behavior and explicit absence of build/test/deploy jobs.

- [ ] **Step 5: Verify without creating a tag**

  Run `sh ci/tests/version-test.sh`,
  `sh ci/tests/pipeline-safe-defaults-test.sh`, `git diff --check`, and inspect
  the workflow to confirm it contains no Java, Maven, Docker or deploy steps.

- [ ] **Step 6: Hand off uncommitted changes**

  Report the changed files and verification evidence. Do not commit, push or
