#!/bin/sh
set -eu

. "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/common.sh"

if ! is_true "${CI_PUSH_IMAGE:-false}"; then
  printf 'Docker push is disabled; set CI_PUSH_IMAGE=true to enable it.\n'
  exit 0
fi
load_ci_env

require_value CI_REGISTRY "${CI_REGISTRY:-}"
require_value CI_REGISTRY_USER "${CI_REGISTRY_USER:-}"
require_value CI_REGISTRY_PASSWORD "${CI_REGISTRY_PASSWORD:-}"
require_value DOCKER_IMAGE_REPO "${DOCKER_IMAGE_REPO:-}"
require_value DOCKER_IMAGE_TAG "${DOCKER_IMAGE_TAG:-}"

printf '%s' "$CI_REGISTRY_PASSWORD" | docker login \
  --username "$CI_REGISTRY_USER" --password-stdin "$CI_REGISTRY"
docker push "$DOCKER_IMAGE_REPO:$DOCKER_IMAGE_TAG"
