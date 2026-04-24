#!/usr/bin/env bash
set -euo pipefail

JAVA17_HOME="${JAVA17_HOME:-$HOME/.local/share/mise/installs/java/17.0.2}"
export JAVA_HOME="$JAVA17_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

CMD="${1:-help}"

case "$CMD" in
  help)
    echo "Usage: scripts/cmd-build.sh [env|clean|assemble|install|test|create]"
    ;;
  env)
    java -version
    gradle -v
    ;;
  clean)
    gradle clean
    ;;
  assemble)
    gradle :app:assembleDebug
    ;;
  install)
    gradle :app:installDebug
    ;;
  test)
    gradle test
    ;;
  create)
    gradle :app:assembleDebug
    echo "APK created at app/build/outputs/apk/debug/app-debug.apk"
    ;;
  *)
    echo "Unknown command: $CMD" >&2
    exit 1
    ;;
esac
