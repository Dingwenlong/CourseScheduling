@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

if /i "%~1"=="--help" goto usage
if /i "%~1"=="-h" goto usage
if /i "%~1"=="/?" goto usage

set "ROOT_DIR=%~dp0.."
set "INIT_SQL=%ROOT_DIR%\database\mysql\init-schema.sql"
set "DEMO_SQL=%ROOT_DIR%\database\mysql\seed-demo-data.sql"
set "MYSQL_CONTAINER=course-scheduling-mysql"
set "RUN_MODE="

if not exist "%INIT_SQL%" (
    echo [ERROR] Init script not found: %INIT_SQL%
    goto fail
)

if not exist "%DEMO_SQL%" (
    echo [ERROR] Demo SQL script not found: %DEMO_SQL%
    goto fail
)

echo ========================================
echo   Course Scheduling Demo Data Seeder
echo ========================================
echo.
echo This script will:
echo   1. Ensure base schema and base data exist
echo   2. Rebuild demo-prefixed timetables
echo   3. Rebuild DEMO-* adjustment applications
echo.
echo Non-demo data will not be deleted.
echo.

cd /d "%ROOT_DIR%"

docker exec %MYSQL_CONTAINER% mysql -uroot -proot123456 -Nse "SELECT 1;" >nul 2>&1
if %errorlevel% equ 0 (
    set "RUN_MODE=docker"
    echo [INFO] Docker MySQL container detected: %MYSQL_CONTAINER%
    echo [INFO] Using containerized MySQL to run the scripts
    echo.
    goto run_docker
)

where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] No available MySQL execution environment was found.
    echo         Please use one of the following options and try again:
    echo         1. Start Docker container %MYSQL_CONTAINER%
    echo         2. Install and configure local mysql client
    goto fail
)

set "RUN_MODE=local"
echo [INFO] Docker MySQL container is not available. Falling back to local mysql client.
echo.

set /p DB_HOST="DB host (default: localhost): "
if "%DB_HOST%"=="" set "DB_HOST=localhost"

set /p DB_PORT="DB port (default: 3306): "
if "%DB_PORT%"=="" set "DB_PORT=3306"

set /p DB_USER="DB user (default: root): "
if "%DB_USER%"=="" set "DB_USER=root"

set /p DB_PASS="DB password (default: root123456): "
if "%DB_PASS%"=="" set "DB_PASS=root123456"

echo.
goto run_local

:run_docker
echo [1/2] Ensuring base schema exists...
type "%INIT_SQL%" | docker exec -i %MYSQL_CONTAINER% mysql --default-character-set=utf8mb4 -uroot -proot123456
if %errorlevel% neq 0 (
    echo [ERROR] Base schema initialization failed
    goto fail
)

echo.
echo [2/2] Writing demo timetables and test data...
type "%DEMO_SQL%" | docker exec -i %MYSQL_CONTAINER% mysql --default-character-set=utf8mb4 -uroot -proot123456
if %errorlevel% neq 0 (
    echo [ERROR] Demo data import failed
    goto fail
)

goto success

:run_local
echo [1/2] Ensuring base schema exists...
mysql --default-character-set=utf8mb4 -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < "%INIT_SQL%"
if %errorlevel% neq 0 (
    echo [ERROR] Base schema initialization failed
    goto fail
)

echo.
echo [2/2] Writing demo timetables and test data...
mysql --default-character-set=utf8mb4 -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < "%DEMO_SQL%"
if %errorlevel% neq 0 (
    echo [ERROR] Demo data import failed
    goto fail
)

goto success

:success
echo.
echo ========================================
echo   Demo Data Import Complete
echo ========================================
echo.
echo Suggested accounts for video demo:
echo   Admin:   admin / 123456
echo   Teacher: teacher001 / 123456
echo   Student: student001 / 123456
echo.
echo Home page and timetable page read the latest timetable of the current semester.
echo This script creates a published timetable with the demo prefix.
echo.
pause
exit /b 0

:fail
echo.
echo Script did not finish successfully. Please review the error above and try again.
echo.
pause
exit /b 1

:usage
echo Usage:
echo   scripts\seed-demo-data.bat
echo.
echo Notes:
echo   1. Tries Docker container course-scheduling-mysql first
echo   2. Falls back to local mysql client if Docker is unavailable
echo   3. Only rebuilds demo-prefixed timetables and DEMO-* applications
echo.
exit /b 0
