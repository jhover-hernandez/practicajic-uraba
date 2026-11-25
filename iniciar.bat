@echo off
REM Enciende PRACTICAJIC URABA: backend, frontend y navegador.
REM Para apagar: cerrar las ventanas "Backend" y "Frontend".

cd /d "%~dp0"

REM Si otra copia del proyecto ya ocupa los puertos, el navegador abriria esa version y no esta.
for %%P in (8080 5173) do (
    netstat -ano | findstr /r /c:":%%P .*LISTENING" >nul && (
        echo.
        echo El puerto %%P ya esta en uso, probablemente por otra copia de PRACTICAJIC que sigue abierta.
        echo Cierra sus ventanas "Backend" y "Frontend" o finaliza los procesos java.exe y node.exe
        echo en el Administrador de tareas, y vuelve a ejecutar este archivo.
        pause
        exit /b 1
    )
)

if not exist "frontend\node_modules" (
    echo Instalando dependencias del frontend por primera vez...
    pushd frontend
    call npm install
    popd
)

start "Backend - PRACTICAJIC" cmd /k "cd /d "%~dp0backend" && set APP_CORREO_HABILITADO=false&& mvn spring-boot:run"
start "Frontend - PRACTICAJIC" cmd /k "cd /d "%~dp0frontend" && npm run dev"

echo Esperando a que el backend termine de arrancar (puede tardar 1 o 2 minutos)...
:esperar
timeout /t 3 /nobreak >nul
curl -s -o nul http://localhost:8080/v3/api-docs
if errorlevel 1 goto esperar

echo Listo. Abriendo el navegador...
start "" http://localhost:5173



REM Administrador inicial en desarrollo: admin@practicajic.local / Admin12345

