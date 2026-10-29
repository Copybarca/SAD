#!/bin/sh
set -eu

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
fixture=$(mktemp -d)
mkdir -p "$fixture/ci"
cp "$PROJECT_ROOT"/ci/*.sh "$fixture/ci/"
git -C "$fixture" init -q -b develop
git -C "$fixture" config user.name ci-test
git -C "$fixture" config user.email ci-test@example.invalid

cat > "$fixture/mvnw" <<'SCRIPT'
#!/bin/sh
printf 'mvnw %s\n' "$*" >> "${CI_TEST_TRACE:?}"
SCRIPT
chmod +x "$fixture/mvnw"
printf 'fixture\n' > "$fixture/file.txt"
git -C "$fixture" add .
git -C "$fixture" commit -q -m fixture

trace="$fixture/trace.log"
(cd "$fixture" && CI_TEST_TRACE="$trace" CI_ENV_FILE="$fixture/build.env" sh ci/pipeline.sh)

grep -q '^mvnw .*verify' "$trace" || { printf 'test stage was not called\n' >&2; exit 1; }
grep -q '^mvnw .*package' "$trace" || { printf 'artifact stage was not called\n' >&2; exit 1; }
test "$(wc -l < "$trace")" -eq 2 || { printf 'unsafe optional stage was called\n' >&2; exit 1; }
test ! -e "$fixture/deploy.executed" || { printf 'deploy was executed\n' >&2; exit 1; }

printf 'pipeline-safe-defaults-test: PASS\n'
