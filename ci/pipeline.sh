#!/bin/sh
set -eu

CI_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

sh "$CI_DIR/test.sh"
sh "$CI_DIR/build-artifact.sh"
sh "$CI_DIR/version.sh"

if [ "${CI_CREATE_TAG:-false}" = true ]; then
  sh "$CI_DIR/auto-tag.sh"
fi
if [ "${CI_BUILD_IMAGE:-false}" = true ]; then
  sh "$CI_DIR/build-image.sh"
fi
if [ "${CI_PUSH_IMAGE:-false}" = true ]; then
  sh "$CI_DIR/push-image.sh"
fi
if [ "${CI_EXECUTE_DEPLOY:-false}" = true ]; then
  sh "$CI_DIR/deploy.sh"
fi
