@echo off
title Compilador APK - AutoChamada
echo ========================================================
echo   Iniciando compilacao do APK (AutoChamada)
echo ========================================================
echo.

where gradle >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [AVISO] O comando 'gradle' global nao foi encontrado.
    if exist gradlew.bat (
        echo Usando gradlew.bat local...
        call gradlew.bat assembleDebug
    ) else (
        echo Para compilar sem instalar nada no PC, suba este projeto no GitHub:
        echo O arquivo .github\workflows\build-apk.yml gerara o APK automaticamente na aba Actions!
        pause
        exit /b 1
    )
) else (
    call gradle assembleDebug
)

echo.
echo Se a compilacao concluiu com sucesso, seu APK esta em:
echo app\build\outputs\apk\debug\app-debug.apk
pause
