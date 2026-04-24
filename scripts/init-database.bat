@echo off
chcp 65001 >nul
echo ========================================
echo   智能排课系统 - 数据库初始化
echo ========================================
echo.

set /p DB_HOST="请输入数据库地址 (默认: localhost): "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="请输入数据库端口 (默认: 3306): "
if "%DB_PORT%"=="" set DB_PORT=3306

set /p DB_USER="请输入数据库用户名 (默认: root): "
if "%DB_USER%"=="" set DB_USER=root

set /p DB_PASS="请输入数据库密码: "

echo.
echo 正在初始化数据库...
echo.

cd /d "%~dp0..\database\mysql"

mysql --default-character-set=utf8mb4 -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < init-schema.sql
if %errorlevel% neq 0 (
    echo 数据库结构创建失败！
    pause
    exit /b 1
)

echo.
echo ========================================
echo   数据库初始化完成！
echo ========================================
echo.
echo 测试账号信息：
echo   管理员: admin / 123456
echo   教师: teacher001 / 123456
echo   学生: student001 / 123456
echo.

pause
