@echo off
REM =======================================================
REM Git 配置設定腳本
REM =======================================================

echo 🔧 設定 Git 配置...
echo.

REM 檢查是否已經設定
for /f "delims=" %%i in ('git config --global user.name 2^>nul') do set GIT_NAME=%%i
for /f "delims=" %%i in ('git config --global user.email 2^>nul') do set GIT_EMAIL=%%i

if defined GIT_NAME if defined GIT_EMAIL (
    echo ✅ Git 已經配置完成:
    echo    姓名: %GIT_NAME%
    echo    Email: %GIT_EMAIL%
    echo.
    goto :check_local
)

echo ❌ Git 尚未配置，請輸入您的資訊:
echo.

REM 設定使用者姓名
if not defined GIT_NAME (
    set /p GIT_NAME="請輸入您的姓名: "
    git config --global user.name "%GIT_NAME%"
)

REM 設定使用者 Email
if not defined GIT_EMAIL (
    set /p GIT_EMAIL="請輸入您的 Email: "
    git config --global user.email "%GIT_EMAIL%"
)

echo.
echo ✅ Git 全域配置完成！

:check_local
REM 檢查本地專案配置
echo.
echo 🔍 檢查本地專案配置...

for /f "delims=" %%i in ('git config user.name 2^>nul') do set LOCAL_NAME=%%i
for /f "delims=" %%i in ('git config user.email 2^>nul') do set LOCAL_EMAIL=%%i

if not defined LOCAL_NAME if not defined LOCAL_EMAIL (
    echo ℹ️  本地專案尚未設定特定配置，將使用全域配置
) else (
    echo ✅ 本地專案配置:
    if defined LOCAL_NAME echo    姓名: %LOCAL_NAME%
    if defined LOCAL_EMAIL echo    Email: %LOCAL_EMAIL%
)

echo.
echo 🎉 Git 配置檢查完成！
echo.

REM 顯示當前配置
echo 📋 當前 Git 配置:
git config --list | findstr user.name
git config --list | findstr user.email

pause