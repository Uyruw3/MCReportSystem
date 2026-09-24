@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ============================================
echo   MCReportPlugin - Aplicar actualizacion
echo ============================================
if not exist "plugins\MCReportPlugin-new.jar" (
    echo [UPDATE] No hay actualizacion pendiente.
    echo          El plugin lo descarga solo al iniciar el servidor.
    pause
    exit /b 0
)
echo [UPDATE] Detectado: plugins\MCReportPlugin-new.jar
if exist "plugins\MCReportPlugin.jar" (
    del /f /q "plugins\MCReportPlugin.jar"
)
ren "plugins\MCReportPlugin-new.jar" "MCReportPlugin.jar"
if exist "plugins\MCReportPlugin.jar" (
    echo [UPDATE] Actualizacion aplicada correctamente.
    echo [UPDATE] Ya puedes iniciar el servidor.
) else (
    echo [UPDATE] ERROR: no se pudo aplicar la actualizacion.
)
pause