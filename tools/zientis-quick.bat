@echo off
setlocal EnableDelayedExpansion

REM =======================================================
REM Zientis CLI 快速版本 - 直接使用 build.bat
REM =======================================================

set VERSION=1.0.0
set TOOLS_DIR=%~dp0
set PROJECT_ROOT=%TOOLS_DIR%..

REM 顏色定義
set RED=[0;31m
set GREEN=[0;32m
set YELLOW=[1;33m
set BLUE=[0;34m
set PURPLE=[0;35m
set CYAN=[0;36m
set NC=[0m

REM 顯示橫幅
echo !PURPLE!
echo  _______ _______ _______ _______ _______ _______ 
echo ^|       ^|_     _^|       ^|       ^|_     _^|       ^|
echo ^|____   ^| ^|   ^| ^|    ___^|    ^|  ^| ^|   ^| ^|  _____^|
echo ^|____^|  ^| ^|   ^| ^|    ___^|       ^| ^|   ^| ^| ^|_____ 
echo  _____^|  ^|_^|   ^|_^|_______^|__^|____^| ^|___^| ^|_______^|
echo.
echo     🚀 Zientis CLI 工具 v%VERSION% (快速版本)
echo !NC!

REM 解析命令
if "%1"=="" goto show_help
if "%1"=="help" goto show_help
if "%1"=="version" goto show_version
if "%1"=="build" goto handle_build

echo !RED!❌ 未知命令: %1!NC!
echo 使用 'zientis-quick help' 查看可用命令。
goto end

:show_help
echo.
echo 使用方式: zientis-quick ^<command^> [options]
echo.
echo 主要命令:
echo   build                編譯相關命令
echo     all               編譯所有模組
echo     core              編譯核心模組  
echo     economy           編譯經濟模組
echo     social            編譯社交模組
echo     watch             監控模式
echo     clean             清理編譯
echo.
echo   version             顯示版本資訊
echo   help                顯示此幫助訊息
echo.
echo 範例:
echo   zientis-quick build all        # 編譯所有模組
echo   zientis-quick build core       # 編譯核心模組
echo   zientis-quick build watch      # 監控模式
echo.
goto end

:show_version
echo !CYAN!Zientis CLI v%VERSION% (快速版本)!NC!
echo 直接使用 build.bat，無 Kotlin 依賴
echo.
goto end

:handle_build
shift
set subcommand=%1
if "%subcommand%"=="" set subcommand=all

echo !GREEN!🚀 使用 build.bat 執行編譯...!NC!

REM 轉換命令參數
set build_args=
if "%subcommand%"=="all" set build_args=-a
if "%subcommand%"=="watch" set build_args=-w
if "%subcommand%"=="clean" set build_args=-c -a
if "%subcommand%"=="core" set build_args=core
if "%subcommand%"=="economy" set build_args=economy
if "%subcommand%"=="social" set build_args=social
if "%subcommand%"=="nations" set build_args=nations
if "%subcommand%"=="multiworld" set build_args=multiworld
if "%subcommand%"=="display" set build_args=display
if "%subcommand%"=="discord-api" set build_args=discord-api

if exist "%TOOLS_DIR%auto-build.sh" (
    echo !BLUE!使用 auto-build.sh 腳本...!NC!
    cd /d "%PROJECT_ROOT%"
    bash "%TOOLS_DIR%auto-build.sh" %build_args% %2 %3 %4 %5
) else if exist "%TOOLS_DIR%build.bat" (
    echo !BLUE!使用 build.bat 腳本...!NC!
    cd /d "%PROJECT_ROOT%"
    call "%TOOLS_DIR%build.bat" %build_args% %2 %3 %4 %5
) else (
    echo !RED!❌ 找不到編譯腳本!NC!
    echo 請確保存在以下檔案之一：
    echo   - %TOOLS_DIR%auto-build.sh
    echo   - %TOOLS_DIR%build.bat
)
goto end

:end
REM 程式結束