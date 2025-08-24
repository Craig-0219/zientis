#!/bin/bash

# =======================================================
# Zientis 自動編譯工具
# =======================================================
# 功能：
# - 自動檢測變更
# - 編譯指定或所有模組
# - 複製JAR到伺服器
# - 支援熱重載測試
# =======================================================

set -e  # 遇到錯誤立即退出

# 配置變數
PROJECT_ROOT="/root/projects/zientis"
MINECRAFT_SERVER_DIR="${PROJECT_ROOT}/minecraft-server"
PLUGINS_DIR="${MINECRAFT_SERVER_DIR}/plugins"
BUILD_CONFIG="${PROJECT_ROOT}/tools/build.conf"

# 顏色輸出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日誌函數
log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# 載入配置
load_config() {
    if [[ -f "$BUILD_CONFIG" ]]; then
        source "$BUILD_CONFIG"
        log "配置檔案已載入: $BUILD_CONFIG"
    else
        warning "配置檔案不存在，使用預設設定"
        create_default_config
    fi
}

# 建立預設配置檔案
create_default_config() {
    cat > "$BUILD_CONFIG" << 'EOF'
# Zientis 自動編譯工具配置

# 模組清單（以空格分隔）
MODULES="zientis-core zientis-economy zientis-multiworld zientis-social zientis-nations zientis-display zientis-discord-api"

# 核心模組（其他模組依賴的基礎模組）
CORE_MODULES="zientis-core"

# 編譯並行數
PARALLEL_JOBS=4

# 是否自動複製到伺服器
AUTO_COPY_TO_SERVER=true

# 是否在複製後自動重啟伺服器
AUTO_RESTART_SERVER=false

# 伺服器重啟等待時間（秒）
SERVER_RESTART_DELAY=5

# 編譯前是否清理
CLEAN_BEFORE_BUILD=false

# 是否只編譯有變更的模組
BUILD_CHANGED_ONLY=true

# 檔案變更檢查時間間隔（秒）
WATCH_INTERVAL=2

# 排除的測試模組
SKIP_TESTS_MODULES="zientis-display"
EOF
    success "預設配置檔案已建立: $BUILD_CONFIG"
}

# 顯示幫助
show_help() {
    cat << 'EOF'
🚀 Zientis 自動編譯工具

用法: ./auto-build.sh [選項] [模組名稱...]

選項:
  -h, --help           顯示此幫助訊息
  -a, --all            編譯所有模組（預設）
  -c, --clean          編譯前先清理
  -w, --watch          監控模式，自動檢測檔案變更並重新編譯
  -s, --skip-copy      跳過複製JAR到伺服器
  -r, --restart        編譯後重啟伺服器
  -t, --test           執行測試
  -f, --force          強制重新編譯所有模組
  -q, --quick          快速模式（跳過測試和文檔）
  -v, --verbose        詳細輸出模式
  --dry-run           乾跑模式，只顯示將執行的操作

模組名稱:
  core                 只編譯核心模組
  economy              只編譯經濟模組
  multiworld           只編譯多世界模組
  social               只編譯社交模組
  nations              只編譯國家模組
  display              只編譯顯示模組
  discord-api          只編譯Discord API模組

範例:
  ./auto-build.sh                    # 編譯所有變更的模組
  ./auto-build.sh -a                 # 強制編譯所有模組
  ./auto-build.sh core economy       # 只編譯核心和經濟模組
  ./auto-build.sh -w                 # 監控模式
  ./auto-build.sh -c -r              # 清理、編譯並重啟伺服器
  ./auto-build.sh -q core            # 快速編譯核心模組

EOF
}

# 檢查依賴
check_dependencies() {
    local deps=("java" "gradle" "find" "rsync")
    for dep in "${deps[@]}"; do
        if ! command -v "$dep" &> /dev/null; then
            error "缺少必要依賴: $dep"
        fi
    done
}

# 檢查模組是否存在
check_module_exists() {
    local module="$1"
    if [[ ! -d "$PROJECT_ROOT/$module" ]]; then
        error "模組不存在: $module"
    fi
}

# 檢測模組變更
detect_changes() {
    local module="$1"
    local module_dir="$PROJECT_ROOT/$module"
    local jar_file="$module_dir/build/libs/$module-${VERSION:-0.2.0-BETA}.jar"
    
    # 如果JAR不存在，視為需要編譯
    if [[ ! -f "$jar_file" ]]; then
        return 0  # 需要編譯
    fi
    
    # 檢查原始碼是否比JAR新
    local newer_files
    newer_files=$(find "$module_dir/src" -name "*.java" -newer "$jar_file" 2>/dev/null | head -1)
    
    if [[ -n "$newer_files" ]]; then
        return 0  # 需要編譯
    else
        return 1  # 不需要編譯
    fi
}

# 編譯單個模組
build_module() {
    local module="$1"
    local skip_tests="${2:-false}"
    local clean="${3:-false}"
    
    log "開始編譯模組: $module"
    
    cd "$PROJECT_ROOT"
    
    local gradle_args=()
    
    # 添加模組前綴
    if [[ "$module" != "root" ]]; then
        gradle_args+=":$module:"
    fi
    
    # 清理選項
    if [[ "$clean" == "true" ]]; then
        gradle_args+=("clean")
    fi
    
    # 編譯目標
    if [[ "$skip_tests" == "true" ]]; then
        gradle_args+=("build")
        gradle_args+=("-x")
        gradle_args+=("test")
    else
        gradle_args+=("build")
    fi
    
    # 執行編譯
    local start_time=$(date +%s)
    
    if [[ "$VERBOSE" == "true" ]]; then
        ./gradlew "${gradle_args[@]}" --info
    else
        ./gradlew "${gradle_args[@]}" --console=plain -q
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    success "模組 $module 編譯完成 (${duration}s)"
}

# 複製JAR到伺服器
copy_to_server() {
    local module="$1"
    local version="${VERSION:-0.2.0-BETA}"
    local jar_file="$PROJECT_ROOT/$module/build/libs/$module-$version.jar"
    
    if [[ ! -f "$jar_file" ]]; then
        warning "JAR檔案不存在: $jar_file"
        return 1
    fi
    
    # 確保插件目錄存在
    mkdir -p "$PLUGINS_DIR"
    
    # 複製JAR
    cp "$jar_file" "$PLUGINS_DIR/"
    success "已複製 $module JAR 到伺服器"
    
    # 記錄複製時間
    touch "$PLUGINS_DIR/.$module.deployed"
}

# 重啟伺服器
restart_server() {
    local server_script="$MINECRAFT_SERVER_DIR/restart-server.sh"
    
    if [[ -f "$server_script" ]]; then
        log "重啟伺服器..."
        cd "$MINECRAFT_SERVER_DIR"
        ./restart-server.sh
    else
        warning "伺服器重啟腳本不存在: $server_script"
        info "請手動重啟伺服器以載入新的JAR檔案"
    fi
}

# 監控模式
watch_mode() {
    log "進入監控模式 - 按 Ctrl+C 退出"
    
    while true; do
        local changed_modules=()
        
        # 檢查每個模組的變更
        for module in $MODULES; do
            if detect_changes "$module"; then
                changed_modules+=("$module")
            fi
        done
        
        # 如果有變更，進行編譯
        if [[ ${#changed_modules[@]} -gt 0 ]]; then
            log "檢測到變更模組: ${changed_modules[*]}"
            build_modules "${changed_modules[@]}"
        fi
        
        sleep "$WATCH_INTERVAL"
    done
}

# 編譯多個模組
build_modules() {
    local modules=("$@")
    local failed_modules=()
    
    log "準備編譯 ${#modules[@]} 個模組: ${modules[*]}"
    
    for module in "${modules[@]}"; do
        check_module_exists "$module"
        
        if [[ "$DRY_RUN" == "true" ]]; then
            info "乾跑模式: 將編譯 $module"
            continue
        fi
        
        # 檢查是否需要編譯
        if [[ "$BUILD_CHANGED_ONLY" == "true" && "$FORCE" != "true" ]]; then
            if ! detect_changes "$module"; then
                info "跳過模組 $module (無變更)"
                continue
            fi
        fi
        
        # 編譯模組
        if build_module "$module" "$QUICK" "$CLEAN_BEFORE_BUILD"; then
            # 複製到伺服器
            if [[ "$AUTO_COPY_TO_SERVER" == "true" && "$SKIP_COPY" != "true" ]]; then
                copy_to_server "$module"
            fi
        else
            failed_modules+=("$module")
        fi
    done
    
    # 報告結果
    if [[ ${#failed_modules[@]} -eq 0 ]]; then
        success "所有模組編譯成功！"
        
        # 重啟伺服器
        if [[ "$AUTO_RESTART_SERVER" == "true" || "$RESTART" == "true" ]]; then
            sleep "$SERVER_RESTART_DELAY"
            restart_server
        fi
    else
        error "以下模組編譯失敗: ${failed_modules[*]}"
    fi
}

# 主函數
main() {
    local target_modules=()
    
    # 解析命令行參數
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -a|--all)
                target_modules=($MODULES)
                shift
                ;;
            -c|--clean)
                CLEAN_BEFORE_BUILD="true"
                shift
                ;;
            -w|--watch)
                WATCH_MODE="true"
                shift
                ;;
            -s|--skip-copy)
                SKIP_COPY="true"
                shift
                ;;
            -r|--restart)
                RESTART="true"
                shift
                ;;
            -t|--test)
                RUN_TESTS="true"
                shift
                ;;
            -f|--force)
                FORCE="true"
                BUILD_CHANGED_ONLY="false"
                shift
                ;;
            -q|--quick)
                QUICK="true"
                shift
                ;;
            -v|--verbose)
                VERBOSE="true"
                shift
                ;;
            --dry-run)
                DRY_RUN="true"
                shift
                ;;
            core)
                target_modules+=("zientis-core")
                shift
                ;;
            economy)
                target_modules+=("zientis-economy")
                shift
                ;;
            multiworld)
                target_modules+=("zientis-multiworld")
                shift
                ;;
            social)
                target_modules+=("zientis-social")
                shift
                ;;
            nations)
                target_modules+=("zientis-nations")
                shift
                ;;
            display)
                target_modules+=("zientis-display")
                shift
                ;;
            discord-api)
                target_modules+=("zientis-discord-api")
                shift
                ;;
            -*)
                error "未知選項: $1"
                ;;
            *)
                # 假設是模組名稱
                target_modules+=("$1")
                shift
                ;;
        esac
    done
    
    # 載入配置
    load_config
    
    # 檢查依賴
    check_dependencies
    
    # 如果沒有指定模組，使用所有模組
    if [[ ${#target_modules[@]} -eq 0 ]]; then
        target_modules=($MODULES)
    fi
    
    # 顯示橫幅
    echo -e "${PURPLE}"
    cat << 'EOF'
 _______ _______ _______ _______ _______ _______ _______
|    ___|_     _|    ___|    |  |_     _|_     _|    ___|
|    ___| |   | |    ___|       | |   | | |   | |    ___|
|_______| |___| |_______|__|____| |___| |_|___|_|_______|
                                                        
    🚀 Zientis 自動編譯工具 v1.0                        
EOF
    echo -e "${NC}"
    
    # 監控模式
    if [[ "$WATCH_MODE" == "true" ]]; then
        watch_mode
        exit 0
    fi
    
    # 編譯模組
    build_modules "${target_modules[@]}"
}

# 執行主函數
main "$@"