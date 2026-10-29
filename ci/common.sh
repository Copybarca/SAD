#!/bin/sh
set -eu

CI_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_ROOT=${CI_PROJECT_DIR:-$(CDPATH= cd -- "$CI_DIR/.." && pwd)}
CI_ENV_FILE=${CI_ENV_FILE:-$PROJECT_ROOT/build.env}
case "$CI_ENV_FILE" in
  /*) ;;
  *) CI_ENV_FILE=$PROJECT_ROOT/$CI_ENV_FILE ;;
esac

is_true() {
  case "${1:-false}" in
    true|TRUE|1|yes|YES) return 0 ;;
    *) return 1 ;;
  esac
}

require_value() {
  variable_name=$1
  variable_value=${2:-}
  if [ -z "$variable_value" ]; then
    printf 'Required variable is not set: %s\n' "$variable_name" >&2
    exit 1
  fi
}

load_ci_env() {
  if [ -f "$CI_ENV_FILE" ]; then
    while IFS= read -r line || [ -n "$line" ]; do
      case "$line" in
        *=*)
          key=${line%%=*}
          value=${line#*=}
          case "$key" in
            VERSION|SHOULD_CREATE_TAG|DOCKER_IMAGE_TAG|DOCKER_IMAGE|SOURCE_BRANCH)
              export "$key=$value"
              ;;
          esac
          ;;
      esac
    done < "$CI_ENV_FILE"
  fi
}
