@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ==============================================
echo   Subiendo PRACTICAJIC URABA a GitHub
echo ==============================================
echo.

where git >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Git no esta instalado. Descargalo de https://git-scm.com/download/win
  pause
  exit /b 1
)

if not exist ".git" (
  git init
)

for /f "delims=" %%e in ('git config user.email') do set GIT_EMAIL=%%e
if "%GIT_EMAIL%"=="" (
  git config user.name "jhover-hernandez"
  git config user.email "jhover-hernandez@users.noreply.github.com"
)

git add .
git commit -m "Estructura inicial: documentacion, prototipo y esquema PostgreSQL"
git branch -M main

git remote get-url origin >nul 2>nul
if errorlevel 1 (
  git remote add origin https://github.com/jhover-hernandez/practicajic-uraba.git
)

echo.
echo Si se abre una ventana de GitHub, inicia sesion con la cuenta jhover-hernandez.
echo.
git push -u origin main

echo.
if errorlevel 1 (
  echo [ERROR] No se pudo subir. Copia el mensaje de arriba y enviaselo a Claude.
) else (
  echo [OK] Listo. Revisa https://github.com/jhover-hernandez/practicajic-uraba
)
pause
