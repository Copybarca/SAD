#!/bin/sh
set -eu

. "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/common.sh"
cd "$PROJECT_ROOT"

exact_tag=$(git tag --points-at HEAD | grep -m1 -E '^[0-9]+\.[0-9]+\.[0-9]+$' || true)
actual_tag=$(git tag --sort=-v:refname | grep -m1 -E '^[0-9]+\.[0-9]+\.[0-9]+$' || true)
source_branch=${CI_SOURCE_BRANCH:-${CI_MERGE_REQUEST_SOURCE_BRANCH_NAME:-}}

if [ -z "$source_branch" ]; then
  source_branch=$(printf '%s\n' "${CI_COMMIT_TITLE:-}" | sed -n "s/^Merge branch '\([^']*\)'.*/\1/p")
fi
if [ -z "$source_branch" ]; then
  source_branch=$(git branch --show-current)
fi

should_create_tag=true
if [ -n "$exact_tag" ]; then
  version=$exact_tag
  should_create_tag=false
elif [ -z "$actual_tag" ]; then
  version=1.0.0
else
  major=${actual_tag%%.*}
  remainder=${actual_tag#*.}
  minor=${remainder%%.*}
  patch=${actual_tag##*.}
  case "$source_branch" in
    feature!/*) version=$((major + 1)).0.0 ;;
    feature/*) version=$major.$((minor + 1)).0 ;;
    fix/*|hotfix/*|perf/*|refactor/*|revert/*) version=$major.$minor.$((patch + 1)) ;;
    doc/*|docs/*|test/*|chore/*|build/*|ci/*)
      version=$actual_tag
      should_create_tag=false
      ;;
    *) version=$major.$minor.$((patch + 1)) ;;
  esac
fi

short_sha=$(git rev-parse --short HEAD)
if [ -n "$exact_tag" ]; then
  image_tag=$exact_tag
else
  image_tag=${version}-${CI_JOB_ID:-local}-${short_sha}
fi

{
  printf 'VERSION=%s\n' "$version"
  printf 'SHOULD_CREATE_TAG=%s\n' "$should_create_tag"
  printf 'DOCKER_IMAGE_TAG=%s\n' "$image_tag"
  printf 'SOURCE_BRANCH=%s\n' "$source_branch"
  if [ -n "${DOCKER_IMAGE_REPO:-}" ]; then
    printf 'DOCKER_IMAGE=%s:%s\n' "$DOCKER_IMAGE_REPO" "$image_tag"
  fi
} > "$CI_ENV_FILE"

printf 'Version: %s; image tag: %s; create tag: %s\n' \
  "$version" "$image_tag" "$should_create_tag"
