#!/bin/sh
set -eu

. "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/common.sh"

if ! is_true "${CI_CREATE_TAG:-false}"; then
  printf 'Tag creation is disabled; set CI_CREATE_TAG=true to enable it.\n'
  exit 0
fi
load_ci_env
if [ "${SHOULD_CREATE_TAG:-false}" != true ]; then
  printf 'Current commit or branch does not require a new tag.\n'
  exit 0
fi

require_value VERSION "${VERSION:-}"
cd "$PROJECT_ROOT"
git tag "$VERSION" HEAD
printf 'Created local tag %s\n' "$VERSION"

if is_true "${CI_PUSH_TAG:-false}"; then
  git push origin "refs/tags/$VERSION"
else
  printf 'Tag push is disabled; set CI_PUSH_TAG=true to enable it.\n'
fi
