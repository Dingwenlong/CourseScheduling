@echo off
setlocal enabledelayedexpansion

echo.
echo ========================================================
echo           Course Scheduling System - Start All
echo ========================================================
echo.

cd /d "%~dp0.."

echo [1/8] Checking Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running, please start Docker Desktop first
    pause
    exit /b 1
)
echo       Docker is ready

echo.
echo [2/8] Starting MySQL and Redis containers...
docker-compose up -d
if %errorlevel% neq 0 (
    echo [ERROR] Docker containers failed to start
    pause
    exit /b 1
)

echo.
echo [3/8] Waiting for MySQL to be ready...
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
echo [4/8] Building backend project...
call mvn clean compile -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Backend build failed
    pause
    exit /b 1
)
echo       Backend build completed

echo.  
echo [5/8] Starting backend service...
start "CourseScheduling-Backend" /min cmd /c "cd /d "%~dp0..\course-scheduling-admin" && mvn spring-boot:run -q"
echo       Backend service starting...
echo       Waiting for backend to initialize (up to 60s)...
set MAX_WAIT=60
set WAITED=0
:wait_backend
netstat -ano | findstr ":8080.*LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo       Backend is ready on port 8080
    goto backend_ready
)
set /a WAITED+=2
if %WAITED% geq %MAX_WAIT% (
    echo [ERROR] Backend startup timeout - check logs for details
    pause
    exit /b 1
)
ping 127.0.0.1 -n 3 >nul
goto wait_backend
:backend_ready

echo.
echo [6/8] Checking frontend dependencies...
cd course-scheduling-web
if not exist "node_modules" (
    echo       Installing frontend dependencies...
    call npm install --silent
    if %errorlevel% neq 0 (
        echo [ERROR] Frontend dependency installation failed
        cd ..
        pause
        exit /b 1
    )
    echo       Frontend dependencies installed
) else (
    echo       Frontend dependencies already installed
)

echo.
echo [7/8] Starting frontend service...
start "CourseScheduling-Frontend" /min cmd /c "npm run dev"
cd ..
echo       Frontend service starting...
echo       Waiting for frontend to initialize (up to 30s)...
set MAX_WAIT=30
set WAITED=0
:wait_frontend
netstat -ano | findstr ":3000.*LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo       Frontend is ready on port 3000
    goto frontend_ready
)
set /a WAITED+=2
if %WAITED% geq %MAX_WAIT% (
    echo [ERROR] Frontend startup timeout - check logs for details
    pause
    exit /b 1
)
ping 127.0.0.1 -n 3 >nul
goto wait_frontend
:frontend_ready

echo.
echo [8/8] Verifying services...
curl -s http://localhost:8080/doc.html >nul 2>&1
if %errorlevel% equ 0 (
    echo       Backend API is responding
) else (
    echo [WARNING] Backend API may not be fully ready yet
)

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
