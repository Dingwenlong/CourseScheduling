@echo off
chcp 65001 >nul
echo ========================================
echo   智能排课系统 - 后端服务启动
echo ========================================
echo.

cd /d "%~dp0.."

echo [1/2] 编译项目...
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo [2/2] 启动服务...
cd "%~dp0..\course-scheduling-admin\target"
if exist "course-scheduling-admin-1.0.0-SNAPSHOT.jar" (
    java -jar course-scheduling-admin-1.0.0-SNAPSHOT.jar
) else (
    echo [ERROR] Jar file not found!
    echo Looking for jar files in current directory:
    dir "*.jar"
    pause
    exit /b 1
)

pause
