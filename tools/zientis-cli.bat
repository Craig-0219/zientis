@echo off
setlocal EnableDelayedExpansion

REM =======================================================
REM Zientis CLI 工具 - Windows 版本
REM =======================================================
REM 整合所有開發工具到單一 CLI 介面
REM =======================================================

set VERSION=1.0.0
set TOOLS_DIR=%~dp0
set PROJECT_ROOT=%TOOLS_DIR%..
set PROJECT_ROOT=%PROJECT_ROOT:\=/%

REM 顏色定義 (Windows 10/11 支援 ANSI)
for /f %%A in ('"prompt $H &echo on &for %%B in (1) do rem"') do set BS=%%A
set RED=[0;31m
set GREEN=[0;32m
set YELLOW=[1;33m
set BLUE=[0;34m
set PURPLE=[0;35m
set CYAN=[0;36m
set NC=[0m

REM 顯示橫幅
:show_banner
echo !PURPLE!
echo  _______ _______ _______ _______ _______ _______ 
echo ^|       ^|_     _^|       ^|       ^|_     _^|       ^|
echo ^|____   ^| ^|   ^| ^|    ___^|    ^|  ^| ^|   ^| ^|  _____^|
echo ^|____^|  ^| ^|   ^| ^|    ___^|       ^| ^|   ^| ^| ^|_____ 
echo  _____^|  ^|_^|   ^|_^|_______^|__^|____^| ^|___^| ^|_______^|
echo.
echo     🚀 Zientis CLI 工具 v%VERSION%
echo !NC!
goto :eof

REM 顯示主要幫助
:show_help
call :show_banner
echo.
echo 使用方式: zientis-cli ^<command^> [options] [arguments]
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
echo     list-backups      列出所有備份
echo     cleanup           清理舊備份
echo.
echo   setup                環境設定命令
echo     init              初始化開發環境
echo     config            配置編輯
echo     check             檢查環境依賴
echo.
echo   demo                 演示功能
echo     run               執行演示
echo.
echo   版本與幫助:
echo     version           顯示版本資訊
echo     help              顯示此幫助訊息
echo.
echo 全域選項:
echo   -v, --verbose       詳細輸出模式
echo   -q, --quiet         安靜模式
echo   -h, --help          顯示幫助
echo   --dry-run          乾跑模式（顯示將執行的操作但不執行）
echo.
echo 範例:
echo   zientis-cli build all              # 編譯所有模組
echo   zientis-cli build core             # 只編譯核心模組
echo   zientis-cli build watch            # 監控模式
echo   zientis-cli deploy status          # 查看部署狀態
echo   zientis-cli deploy all             # 部署所有模組
echo   zientis-cli deploy rollback core   # 回滾核心模組
echo   zientis-cli setup init             # 初始化環境
echo.
echo 詳細說明請使用: zientis-cli ^<command^> --help
echo.
goto :eof

REM 顯示版本資訊
:show_version
echo !CYAN!Zientis CLI v%VERSION%!NC!
echo 編譯工具整合介面 - Windows 版本
echo.
echo 可用工具:
echo   - 自動編譯工具 (build.bat)
echo   - 部署管理工具 (deploy-runner.main.kts)
echo   - 環境設定工具 (setup-runner.main.kts)
echo   - 演示工具 (demo-runner.main.kts)
goto :eof

REM 執行 Kotlin 腳本
:run_kotlin_script
set script=%1
shift
set script_path=%TOOLS_DIR%%script%

if not exist "%script_path%" (
    echo !RED!❌ 腳本不存在: %script%!NC!
    exit /b 1
)

REM 檢查 kotlin 是否可用
kotlin -version >nul 2>&1
if errorlevel 1 (
    echo !YELLOW!⚠️  Kotlin 未安裝，嘗試使用替代方案...!NC!
    
    REM 對於編譯，回退到 build.bat
    if "%script%"=="build-runner.main.kts" (
        call :run_batch_script "build.bat" %*
        goto :eof
    ) else (
        echo !RED!❌ 此功能需要安裝 Kotlin。請安裝 Kotlin 後重試。!NC!
        exit /b 1
    )
)

cd /d "%PROJECT_ROOT%"
kotlin -s "%script_path%" %*
goto :eof

REM 執行批次檔案
:run_batch_script
set script=%~1
shift
set script_path=%TOOLS_DIR%%script%

if not exist "%script_path%" (
    echo !RED!❌ 腳本不存在: %script%!NC!
    exit /b 1
)

cd /d "%PROJECT_ROOT%"
call "%script_path%" %*
goto :eof

REM 處理 build 命令
:handle_build
set subcommand=%1
if "%subcommand%"=="" set subcommand=all
if "%subcommand%"=="help" goto :build_help
if "%subcommand%"=="--help" goto :build_help
if "%subcommand%"=="-h" goto :build_help

if "%subcommand%"=="all" (
    shift
    call :run_kotlin_script "build-runner.main.kts" --all %*
) else if "%subcommand%"=="watch" (
    shift
    call :run_kotlin_script "build-runner.main.kts" --watch %*
) else if "%subcommand%"=="clean" (
    shift
    call :run_kotlin_script "build-runner.main.kts" --clean --all %*
) else if "%subcommand%"=="core" (
    shift
    call :run_kotlin_script "build-runner.main.kts" core %*
) else if "%subcommand%"=="economy" (
    shift
    call :run_kotlin_script "build-runner.main.kts" economy %*
) else if "%subcommand%"=="social" (
    shift
    call :run_kotlin_script "build-runner.main.kts" social %*
) else if "%subcommand%"=="nations" (
    shift
    call :run_kotlin_script "build-runner.main.kts" nations %*
) else if "%subcommand%"=="multiworld" (
    shift
    call :run_kotlin_script "build-runner.main.kts" multiworld %*
) else if "%subcommand%"=="display" (
    shift
    call :run_kotlin_script "build-runner.main.kts" display %*
) else if "%subcommand%"=="discord-api" (
    shift
    call :run_kotlin_script "build-runner.main.kts" discord-api %*
) else (
    call :run_kotlin_script "build-runner.main.kts" %*
)
goto :eof

:build_help
echo 編譯命令使用方式: zientis-cli build ^<subcommand^> [options]
echo.
echo 子命令:
echo   all                  編譯所有模組 (預設)
echo   ^<module^>             編譯指定模組
echo   watch               監控模式，自動重新編譯
echo   clean               清理編譯快取
echo.
echo 模組名稱:
echo   core                zientis-core
echo   economy             zientis-economy
echo   social              zientis-social
echo   nations             zientis-nations
echo   multiworld          zientis-multiworld
echo   display             zientis-display
echo   discord-api         zientis-discord-api
echo.
echo 選項:
echo   -c, --clean         編譯前先清理
echo   -q, --quick         快速模式（跳過測試）
echo   -f, --force         強制重新編譯
echo   -r, --restart       編譯後重啟伺服器
echo   -v, --verbose       詳細輸出
echo   --dry-run          乾跑模式
echo.
echo 範例:
echo   zientis-cli build all              # 編譯所有模組
echo   zientis-cli build core --clean     # 清理並編譯核心模組
echo   zientis-cli build watch            # 監控模式
echo   zientis-cli build --quick --force  # 快速強制編譯所有模組
goto :eof

REM 處理 deploy 命令
:handle_deploy
set subcommand=%1
if "%subcommand%"=="" goto :deploy_help
if "%subcommand%"=="help" goto :deploy_help
if "%subcommand%"=="--help" goto :deploy_help
if "%subcommand%"=="-h" goto :deploy_help

if "%subcommand%"=="status" (
    shift
    call :run_kotlin_script "deploy-runner.main.kts" status %*
) else if "%subcommand%"=="all" (
    shift
    call :run_kotlin_script "deploy-runner.main.kts" deploy-all %*
) else if "%subcommand%"=="rollback" (
    shift
    if "%1"=="" (
        echo !RED!❌ 請指定要回滾的模組名稱!NC!
        exit /b 1
    )
    call :run_kotlin_script "deploy-runner.main.kts" rollback %*
) else if "%subcommand%"=="list-backups" (
    shift
    call :run_kotlin_script "deploy-runner.main.kts" list-backups %*
) else if "%subcommand%"=="cleanup" (
    shift
    call :run_kotlin_script "deploy-runner.main.kts" cleanup %*
) else (
    call :run_kotlin_script "deploy-runner.main.kts" deploy %*
)
goto :eof

:deploy_help
echo 部署命令使用方式: zientis-cli deploy ^<subcommand^> [options]
echo.
echo 子命令:
echo   status              查看部署狀態
echo   ^<module^>            部署指定模組
echo   all                 部署所有模組
echo   rollback ^<module^>   回滾指定模組
echo   list-backups        列出所有備份
echo   cleanup             清理舊備份
echo.
echo 選項:
echo   -f, --force         強制部署
echo   -v, --verbose       詳細輸出
echo   --dry-run          乾跑模式
echo.
echo 範例:
echo   zientis-cli deploy status          # 查看狀態
echo   zientis-cli deploy core            # 部署核心模組
echo   zientis-cli deploy all             # 部署所有模組
echo   zientis-cli deploy rollback core   # 回滾核心模組
goto :eof

REM 處理 setup 命令
:handle_setup
set subcommand=%1
if "%subcommand%"=="" goto :setup_help
if "%subcommand%"=="help" goto :setup_help
if "%subcommand%"=="--help" goto :setup_help
if "%subcommand%"=="-h" goto :setup_help

if "%subcommand%"=="init" (
    shift
    call :run_kotlin_script "setup-runner.main.kts" init %*
) else if "%subcommand%"=="config" (
    shift
    call :run_kotlin_script "setup-runner.main.kts" config %*
) else if "%subcommand%"=="check" (
    shift
    call :run_kotlin_script "setup-runner.main.kts" check %*
) else (
    call :run_kotlin_script "setup-runner.main.kts" %*
)
goto :eof

:setup_help
echo 設定命令使用方式: zientis-cli setup ^<subcommand^> [options]
echo.
echo 子命令:
echo   init                初始化開發環境
echo   config              編輯配置檔案
echo   check               檢查環境依賴
echo.
echo 範例:
echo   zientis-cli setup init             # 初始化環境
echo   zientis-cli setup check            # 檢查依賴
goto :eof

REM 處理 demo 命令
:handle_demo
set subcommand=%1
if "%subcommand%"=="" set subcommand=run
if "%subcommand%"=="help" goto :demo_help
if "%subcommand%"=="--help" goto :demo_help
if "%subcommand%"=="-h" goto :demo_help

call :run_kotlin_script "demo-runner.main.kts" %*
goto :eof

:demo_help
echo 演示命令使用方式: zientis-cli demo [subcommand] [options]
echo.
echo 子命令:
echo   run                 執行演示 (預設)
echo.
echo 範例:
echo   zientis-cli demo run               # 執行演示
goto :eof

REM 主函數
:main
set verbose=false
set quiet=false
set dry_run=false

REM 解析參數
:parse_args
if "%1"=="" goto :no_args
if "%1"=="-v" goto :set_verbose
if "%1"=="--verbose" goto :set_verbose
if "%1"=="-q" goto :set_quiet
if "%1"=="--quiet" goto :set_quiet
if "%1"=="--dry-run" goto :set_dry_run
if "%1"=="-h" goto :show_help_exit
if "%1"=="--help" goto :show_help_exit
if "%1"=="help" goto :show_help_exit
if "%1"=="version" goto :show_version_exit
if "%1"=="--version" goto :show_version_exit
if "%1"=="build" goto :handle_build_args
if "%1"=="deploy" goto :handle_deploy_args
if "%1"=="setup" goto :handle_setup_args
if "%1"=="demo" goto :handle_demo_args

echo !RED!❌ 未知命令: %1。使用 'zientis-cli help' 查看可用命令。!NC!
exit /b 1

:set_verbose
set verbose=true
set VERBOSE=true
shift
goto :parse_args

:set_quiet
set quiet=true
set QUIET=true
shift
goto :parse_args

:set_dry_run
set dry_run=true
set DRY_RUN=true
shift
goto :parse_args

:show_help_exit
call :show_help
exit /b 0

:show_version_exit
call :show_version
exit /b 0

:handle_build_args
shift
call :handle_build %*
exit /b %errorlevel%

:handle_deploy_args
shift
call :handle_deploy %*
exit /b %errorlevel%

:handle_setup_args
shift
call :handle_setup %*
exit /b %errorlevel%

:handle_demo_args
shift
call :handle_demo %*
exit /b %errorlevel%

:no_args
call :show_help
exit /b 0

REM 入口點
call :main %*