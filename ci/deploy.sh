#!/bin/sh
set -eu

. "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/common.sh"

if ! is_true "${CI_EXECUTE_DEPLOY:-false}"; then
  printf 'Deploy is disabled. Future hook: set CI_EXECUTE_DEPLOY=true and DEPLOY_COMMAND.\n'
  exit 0
fi
load_ci_env

require_value DEPLOY_COMMAND "${DEPLOY_COMMAND:-}"
printf 'Executing explicitly enabled deploy hook.\n'
sh -c "$DEPLOY_COMMAND"
