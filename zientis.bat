@echo off
REM =======================================================
REM Zientis CLI Windows 啟動器
REM =======================================================

set SCRIPT_DIR=%~dp0
set TOOLS_DIR=%SCRIPT_DIR%tools

if exist "%TOOLS_DIR%\zientis-cli.bat" (
    call "%TOOLS_DIR%\zientis-cli.bat" %*
) else (
    echo 錯誤: 找不到 zientis-cli.bat
    echo 請確保 %TOOLS_DIR%\zientis-cli.bat 存在
    exit /b 1
)