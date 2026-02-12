@echo off
chcp 65001 >nul
echo ========================================
echo   智能排课系统 - 前端开发服务启动
echo ========================================
echo.

cd /d "%~dp0..\course-scheduling-web"

echo 启动前端开发服务器...
call npm run dev

pause
