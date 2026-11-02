#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
find src/main/java -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d out
exec java -cp out chess.Main
