@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================================
echo           Course Scheduling System - Start All
echo ========================================================
echo.

cd /d "%~dp0.."

echo [1/6] Checking Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running, please start Docker Desktop first
    pause
    exit /b 1
)
echo       Docker is ready

echo.
echo [2/6] Starting MySQL and Redis containers...
docker-compose up -d
if %errorlevel% neq 0 (
    echo [ERROR] Docker containers failed to start
    pause
    exit /b 1
)

echo.
echo [3/6] Waiting for MySQL to be ready...
set MAX_WAIT=60
set WAITED=0
:wait_mysql
docker exec course-scheduling-mysql mysqladmin ping -h localhost -uroot -proot123456 --silent 2>nul
if %errorlevel% equ 0 (
    echo       MySQL is ready
    goto mysql_ready
)
set /a WAITED+=2
if %WAITED% geq %MAX_WAIT% (
    echo [ERROR] MySQL startup timeout
    pause
    exit /b 1
)
ping 127.0.0.1 -n 3 >nul
goto wait_mysql
:mysql_ready

echo.
echo [4/6] Building backend project...
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Backend build failed
    pause
    exit /b 1
)
echo       Backend build completed

echo.  
echo [5/6] Starting backend service...
cd "%~dp0..\course-scheduling-admin\target"
if exist "course-scheduling-admin-1.0.0-SNAPSHOT.jar" (
    start "CourseScheduling-Backend" /min cmd /c "echo Starting backend service... & java -jar course-scheduling-admin-1.0.0-SNAPSHOT.jar"
) else (
    echo [ERROR] Backend jar file not found!
    pause
    exit /b 1
)
cd "%~dp0.."
echo       Backend service starting...
echo       Waiting for backend to initialize...
ping 127.0.0.1 -n 10 >nul

echo.
echo [6/6] Starting frontend service...
cd course-scheduling-web
start "CourseScheduling-Frontend" /min cmd /c "npm run dev"
cd ..
echo       Frontend service starting...
ping 127.0.0.1 -n 4 >nul

echo.
echo ========================================================
echo                    Started Successfully!
echo ========================================================
echo.
echo   Frontend:  http://localhost:3000
echo   Backend:   http://localhost:8080
echo   API Docs:  http://localhost:8080/doc.html
echo.
echo ========================================================
echo   Test Accounts:
echo     Admin:    admin / 123456
echo     Teacher:  teacher001 / 123456
echo     Student:  student001 / 123456
echo.
echo ========================================================
echo   Database:
echo     MySQL:    localhost:3306  root / root123456
echo     Redis:    localhost:6379
echo.
echo ========================================================
echo.
echo NOTE: Closing this window will NOT stop the services.
echo        Run stop-all.bat to stop all services.
echo.

pause
