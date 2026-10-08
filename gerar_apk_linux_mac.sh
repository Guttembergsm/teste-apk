#!/usr/bin/env bash
set -e

echo "========================================================"
echo "  Compilando APK Debug - AutoChamada"
echo "========================================================"

if command -v gradle >/dev/null 2>&1; then
  gradle assembleDebug --stacktrace
elif [ -f "./gradlew" ]; then
  chmod +x ./gradlew
  ./gradlew assembleDebug --stacktrace
else
  echo "Instalando Gradle temporariamente via SDKMAN ou apt..."
  sudo apt-get update && sudo apt-get install -y gradle openjdk-17-jdk
  gradle assembleDebug --stacktrace
fi

echo ""
echo "✅ APK gerado com sucesso em:"
echo "   app/build/outputs/apk/debug/app-debug.apk"
