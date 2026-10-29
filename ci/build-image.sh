#!/bin/sh
set -eu

. "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/common.sh"

if ! is_true "${CI_BUILD_IMAGE:-false}"; then
  printf 'Docker build is disabled; set CI_BUILD_IMAGE=true to enable it.\n'
  exit 0
fi
load_ci_env

require_value DOCKER_IMAGE_REPO "${DOCKER_IMAGE_REPO:-}"
require_value DOCKER_IMAGE_TAG "${DOCKER_IMAGE_TAG:-}"
cd "$PROJECT_ROOT"
docker build --tag "$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_TAG" .
