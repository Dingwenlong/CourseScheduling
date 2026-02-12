@echo off

echo.
echo ========================================================
echo           Course Scheduling System - Stop All
echo ========================================================
echo.

cd /d "%~dp0.."

echo [1/3] Stopping frontend service...
taskkill /fi "WINDOWTITLE eq CourseScheduling-Frontend*" /f >nul 2>&1
echo       Frontend stopped

echo.
echo [2/3] Stopping backend service...
taskkill /fi "WINDOWTITLE eq CourseScheduling-Backend*" /f >nul 2>&1
echo       Backend stopped

echo.
echo [3/3] Stopping Docker containers...
docker-compose down
echo       Docker containers stopped

echo.
echo ========================================================
echo                    All Services Stopped
echo ========================================================
echo.

pause
