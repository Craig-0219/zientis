@echo off
setlocal EnableDelayedExpansion

REM =======================================================
REM Zientis CLI 工具 - Windows 版本 (修復版)
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
echo     🚀 Zientis CLI 工具 v%VERSION%
echo !NC!

REM 解析命令
if "%1"=="" goto show_help
if "%1"=="help" goto show_help
if "%1"=="--help" goto show_help
if "%1"=="-h" goto show_help
if "%1"=="version" goto show_version
if "%1"=="--version" goto show_version
if "%1"=="build" goto handle_build
if "%1"=="deploy" goto handle_deploy
if "%1"=="setup" goto handle_setup
if "%1"=="demo" goto handle_demo

echo !RED!❌ 未知命令: %1!NC!
echo 使用 'zientis help' 查看可用命令。
goto end

:show_help
echo.
echo 使用方式: zientis ^<command^> [options] [arguments]
echo.
echo 主要命令:
echo   build                編譯相關命令
echo     all               編譯所有模組
echo     ^<module^>          編譯指定模組 (core, economy, social 等)
echo     watch             監控模式，自動重新編譯變更的模組
echo     clean             清理編譯快取
echo.
echo   deploy               部署相關命令
echo     status            查看部署狀態
echo     ^<module^>          部署指定模組
echo     all               部署所有模組
echo     rollback ^<module^> 回滾指定模組到上一版本
echo.
echo   setup                環境設定命令
echo     init              初始化開發環境
echo     check             檢查環境依賴
echo.
echo   demo                 演示功能
echo     run               執行演示
echo.
echo   版本與幫助:
echo     version           顯示版本資訊
echo     help              顯示此幫助訊息
echo.
echo 範例:
echo   zientis build all              # 編譯所有模組
echo   zientis build core             # 只編譯核心模組
echo   zientis build watch            # 監控模式
echo   zientis deploy status          # 查看部署狀態
echo.
echo 詳細說明請使用: zientis ^<command^> help
echo.
goto end

:show_version
echo !CYAN!Zientis CLI v%VERSION%!NC!
echo 編譯工具整合介面 - Windows 版本
echo.
echo 可用工具:
echo   - 自動編譯工具 (build.bat)
echo   - 部署管理工具 (deploy-runner.main.kts)
echo   - 環境設定工具 (setup-runner.main.kts)  
echo   - 演示工具 (demo-runner.main.kts)
echo.
goto end

:handle_build
shift
set subcommand=%1
if "%subcommand%"=="" set subcommand=all
if "%subcommand%"=="help" goto build_help

echo !YELLOW!⚠️  檢查 Kotlin 是否可用...!NC!

REM 快速檢查 Kotlin 命令是否存在
where kotlin >nul 2>&1
if errorlevel 1 (
    echo !YELLOW!⚠️  Kotlin 未安裝，使用 build.bat 替代!NC!
    if exist "%TOOLS_DIR%build.bat" (
        cd /d "%PROJECT_ROOT%"
        call "%TOOLS_DIR%build.bat" --%subcommand% %2 %3 %4 %5
    ) else (
        echo !RED!❌ 找不到 build.bat!NC!
    )
) else (
    REM 嘗試執行 Kotlin，但限制執行時間
    echo !GREEN!✅ 找到 Kotlin，執行編譯腳本...!NC!
    cd /d "%PROJECT_ROOT%"
    timeout /t 1 >nul
    kotlin -s "%TOOLS_DIR%build-runner.main.kts" --%subcommand% %2 %3 %4 %5
)
goto end

:build_help
echo 編譯命令使用方式: zientis build ^<subcommand^> [options]
echo.
echo 子命令:
echo   all                  編譯所有模組 (預設)
echo   ^<module^>             編譯指定模組
echo   watch               監控模式，自動重新編譯
echo   clean               清理編譯快取
echo.
echo 模組名稱:
echo   core, economy, social, nations, multiworld, display, discord-api
echo.
echo 選項:
echo   -c, --clean         編譯前先清理
echo   -q, --quick         快速模式（跳過測試）
echo   -f, --force         強制重新編譯
echo   -v, --verbose       詳細輸出
echo   --dry-run          乾跑模式
echo.
goto end

:handle_deploy
shift
set subcommand=%1
if "%subcommand%"=="" set subcommand=status
if "%subcommand%"=="help" goto deploy_help

echo !BLUE!🚀 執行部署命令: %subcommand%!NC!
cd /d "%PROJECT_ROOT%"
kotlin -s "%TOOLS_DIR%deploy-runner.main.kts" %subcommand% %2 %3 %4 %5
goto end

:deploy_help
echo 部署命令使用方式: zientis deploy ^<subcommand^> [options]
echo.
echo 子命令:
echo   status              查看部署狀態 (預設)
echo   ^<module^>            部署指定模組
echo   all                 部署所有模組
echo   rollback ^<module^>   回滾指定模組
echo.
goto end

:handle_setup
shift
set subcommand=%1
if "%subcommand%"=="" set subcommand=check
if "%subcommand%"=="help" goto setup_help

echo !BLUE!🔧 執行環境設定: %subcommand%!NC!
cd /d "%PROJECT_ROOT%"
kotlin -s "%TOOLS_DIR%setup-runner.main.kts" %subcommand% %2 %3 %4 %5
goto end

:setup_help
echo 設定命令使用方式: zientis setup ^<subcommand^> [options]
echo.
echo 子命令:
echo   check               檢查環境依賴 (預設)
echo   init                初始化開發環境
echo   config              編輯配置檔案
echo.
goto end

:handle_demo
shift
echo !BLUE!🎯 執行演示功能!NC!
cd /d "%PROJECT_ROOT%"
kotlin -s "%TOOLS_DIR%demo-runner.main.kts" %1 %2 %3 %4 %5
goto end

:end
REM 程式結束