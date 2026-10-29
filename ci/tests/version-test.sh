#!/bin/sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
VERSION_SCRIPT="$PROJECT_ROOT/ci/version.sh"
AUTO_TAG_SCRIPT="$PROJECT_ROOT/ci/auto-tag.sh"

fail() {
  printf 'FAIL: %s\n' "$1" >&2
  exit 1
}

assert_env() {
  file=$1
  expected=$2
  grep -qx "$expected" "$file" || fail "expected $expected in $file"
}

new_repo() {
  repo=$(mktemp -d)
  git -C "$repo" init -q -b develop
  git -C "$repo" config user.name ci-test
  git -C "$repo" config user.email ci-test@example.invalid
  printf 'initial\n' > "$repo/file.txt"
  git -C "$repo" add file.txt
  git -C "$repo" commit -q -m initial
  printf '%s\n' "$repo"
}

repo=$(new_repo)
(cd "$repo" && CI_PROJECT_DIR="$repo" CI_ENV_FILE=build.env CI_SOURCE_BRANCH=feature/first sh "$VERSION_SCRIPT")
assert_env "$repo/build.env" 'VERSION=1.0.0'
assert_env "$repo/build.env" 'SHOULD_CREATE_TAG=true'

repo=$(new_repo)
git -C "$repo" tag 1.2.3
printf 'feature\n' >> "$repo/file.txt"
git -C "$repo" commit -qam feature
(cd "$repo" && CI_PROJECT_DIR="$repo" CI_ENV_FILE=build.env CI_SOURCE_BRANCH=feature/history sh "$VERSION_SCRIPT")
assert_env "$repo/build.env" 'VERSION=1.3.0'

repo=$(new_repo)
git -C "$repo" tag 1.2.3
printf 'fix\n' >> "$repo/file.txt"
git -C "$repo" commit -qam fix
(cd "$repo" && CI_PROJECT_DIR="$repo" CI_ENV_FILE=build.env CI_SOURCE_BRANCH=fix/history sh "$VERSION_SCRIPT")
assert_env "$repo/build.env" 'VERSION=1.2.4'

repo=$(new_repo)
git -C "$repo" tag 1.2.3
printf 'docs\n' >> "$repo/file.txt"
git -C "$repo" commit -qam docs
(cd "$repo" && CI_PROJECT_DIR="$repo" CI_ENV_FILE=build.env CI_SOURCE_BRANCH=docs/readme sh "$VERSION_SCRIPT")
assert_env "$repo/build.env" 'VERSION=1.2.3'
assert_env "$repo/build.env" 'SHOULD_CREATE_TAG=false'

repo=$(new_repo)
git -C "$repo" checkout -q -b 'feature/x;printf${IFS}CI_INJECTION_MARKER'
CI_PROJECT_DIR="$repo" CI_ENV_FILE="$repo/build.env" \
  CI_SOURCE_BRANCH='feature/x;printf${IFS}CI_INJECTION_MARKER' sh "$VERSION_SCRIPT" >/dev/null
output=$(cd "$repo" && CI_PROJECT_DIR="$repo" CI_ENV_FILE="$repo/build.env" \
  CI_CREATE_TAG=false sh "$AUTO_TAG_SCRIPT")
case "$output" in
  *CI_INJECTION_MARKER*) fail 'generated metadata executed branch name as shell code' ;;
esac

printf 'version-test: PASS\n'
