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
cd course-scheduling-admin\target
java -jar course-scheduling-admin-1.0.0-SNAPSHOT.jar

pause
