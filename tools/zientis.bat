@echo off
REM =======================================================
REM Zientis CLI - tools 目錄啟動器
REM =======================================================

set TOOLS_DIR=%~dp0
set PROJECT_ROOT=%TOOLS_DIR%..

REM 檢查是否存在 zientis-cli.bat
if exist "%TOOLS_DIR%zientis-cli.bat" (
    call "%TOOLS_DIR%zientis-cli.bat" %*
) else (
    echo 錯誤: 找不到 zientis-cli.bat
    echo 請確保 %TOOLS_DIR%zientis-cli.bat 存在
    exit /b 1
)