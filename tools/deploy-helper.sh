#!/bin/bash

# =======================================================
# Zientis JAR 部署輔助工具
# =======================================================
# 功能：
# - 智慧JAR複製和備份
# - 伺服器熱重載支援
# - 版本管理
# - 回滾功能
# =======================================================

set -e

# 匯入主配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_CONFIG="$SCRIPT_DIR/build.conf"

if [[ -f "$BUILD_CONFIG" ]]; then
    source "$BUILD_CONFIG"
fi

# 顏色輸出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 日誌函數
log() { echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"; }
success() { echo -e "${GREEN}✅ $1${NC}"; }
warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
error() { echo -e "${RED}❌ $1${NC}"; exit 1; }
info() { echo -e "${CYAN}ℹ️  $1${NC}"; }

# 獲取JAR檔案資訊
get_jar_info() {
    local module="$1"
    local version="${PROJECT_VERSION:-0.2.0-BETA}"
    local jar_path="$PROJECT_ROOT/$module/build/libs/$module-$version.jar"
    
    if [[ -f "$jar_path" ]]; then
        echo "$jar_path"
    else
        return 1
    fi
}

# 獲取伺服器插件路徑
get_server_plugin_path() {
    local module="$1"
    local version="${PROJECT_VERSION:-0.2.0-BETA}"
    local server_plugins="${PROJECT_ROOT}/${PLUGINS_RELATIVE_PATH:-minecraft-server/plugins}"
    
    echo "$server_plugins/$module-$version.jar"
}

# 檢查JAR檔案版本
check_jar_version() {
    local jar_file="$1"
    
    if [[ -f "$jar_file" ]]; then
        # 使用 jar 命令檢查 MANIFEST.MF
        local version
        version=$(jar -xf "$jar_file" META-INF/MANIFEST.MF 2>/dev/null && \
                 grep -i "implementation-version" META-INF/MANIFEST.MF | \
                 cut -d':' -f2 | tr -d ' ' 2>/dev/null || echo "unknown")
        rm -f META-INF/MANIFEST.MF 2>/dev/null
        echo "$version"
    else
        echo "not_found"
    fi
}

# 計算檔案雜湊值
calculate_hash() {
    local file="$1"
    if [[ -f "$file" ]]; then
        sha256sum "$file" | cut -d' ' -f1
    else
        echo "no_file"
    fi
}

# 備份現有JAR
backup_existing_jar() {
    local target_jar="$1"
    local module="$2"
    
    if [[ -f "$target_jar" ]]; then
        local backup_dir="${PROJECT_ROOT}/${BACKUP_DIR:-tools/backups}"
        mkdir -p "$backup_dir"
        
        local timestamp=$(date +%Y%m%d_%H%M%S)
        local backup_name="${module}_${timestamp}.jar"
        local backup_path="$backup_dir/$backup_name"
        
        cp "$target_jar" "$backup_path"
        success "已備份現有JAR: $backup_name"
        
        # 清理舊備份
        cleanup_old_backups "$backup_dir" "$module"
        
        echo "$backup_path"
    fi
}

# 清理舊備份
cleanup_old_backups() {
    local backup_dir="$1"
    local module="$2"
    local retention=${BACKUP_RETENTION_COUNT:-5}
    
    # 找出該模組的所有備份檔案，按時間排序，刪除超出保留數量的檔案
    find "$backup_dir" -name "${module}_*.jar" -type f | \
    sort -r | \
    tail -n +$((retention + 1)) | \
    while read -r old_backup; do
        rm -f "$old_backup"
        info "已刪除舊備份: $(basename "$old_backup")"
    done
}

# 部署JAR檔案
deploy_jar() {
    local module="$1"
    local force="${2:-false}"
    
    local source_jar
    source_jar=$(get_jar_info "$module") || {
        error "找不到模組 $module 的JAR檔案"
    }
    
    local target_jar
    target_jar=$(get_server_plugin_path "$module")
    
    # 確保目標目錄存在
    mkdir -p "$(dirname "$target_jar")"
    
    # 檢查是否需要更新
    if [[ "$force" != "true" ]]; then
        local source_hash target_hash
        source_hash=$(calculate_hash "$source_jar")
        target_hash=$(calculate_hash "$target_jar")
        
        if [[ "$source_hash" == "$target_hash" ]]; then
            info "模組 $module JAR 無變更，跳過部署"
            return 0
        fi
    fi
    
    # 備份現有JAR（如果啟用）
    if [[ "${BACKUP_BEFORE_DEPLOY:-true}" == "true" ]]; then
        backup_existing_jar "$target_jar" "$module"
    fi
    
    # 執行預部署Hook
    if [[ -n "${PRE_DEPLOY_HOOK:-}" && -x "$PRE_DEPLOY_HOOK" ]]; then
        log "執行預部署Hook: $PRE_DEPLOY_HOOK"
        "$PRE_DEPLOY_HOOK" "$module" "$source_jar" "$target_jar"
    fi
    
    # 複製JAR檔案
    cp "$source_jar" "$target_jar"
    
    # 設置檔案權限
    chmod 644 "$target_jar"
    
    # 記錄部署資訊
    local deploy_info_file="$(dirname "$target_jar")/.$module.deploy-info"
    cat > "$deploy_info_file" << EOF
DEPLOY_TIME=$(date '+%Y-%m-%d %H:%M:%S')
MODULE=$module
VERSION=$PROJECT_VERSION
SOURCE_PATH=$source_jar
TARGET_PATH=$target_jar
SOURCE_HASH=$(calculate_hash "$source_jar")
DEPLOYED_BY=$USER
EOF
    
    success "模組 $module JAR 部署完成"
    
    # 執行後部署Hook
    if [[ -n "${POST_DEPLOY_HOOK:-}" && -x "$POST_DEPLOY_HOOK" ]]; then
        log "執行後部署Hook: $POST_DEPLOY_HOOK"
        "$POST_DEPLOY_HOOK" "$module" "$source_jar" "$target_jar"
    fi
    
    # 熱重載支援
    if [[ "${HOT_RELOAD_MODULES:-}" =~ $module ]]; then
        attempt_hot_reload "$module"
    fi
}

# 嘗試熱重載
attempt_hot_reload() {
    local module="$1"
    local delay="${HOT_RELOAD_DELAY:-3}"
    
    info "嘗試熱重載模組: $module"
    
    # 等待一段時間讓伺服器檢測檔案變更
    sleep "$delay"
    
    # 發送重載命令到伺服器（如果支援）
    local reload_command="zientis reload $module"
    send_server_command "$reload_command" || {
        warning "熱重載失敗，可能需要重啟伺服器"
    }
}

# 發送命令到伺服器
send_server_command() {
    local command="$1"
    local server_dir="${PROJECT_ROOT}/${MINECRAFT_SERVER_RELATIVE_PATH:-minecraft-server}"
    local server_input="$server_dir/server.input"
    
    # 檢查伺服器是否在運行
    if pgrep -f "paper.*jar" > /dev/null; then
        # 如果存在輸入管道，發送命令
        if [[ -p "$server_input" ]]; then
            echo "$command" > "$server_input"
            success "已發送命令到伺服器: $command"
            return 0
        fi
    fi
    
    return 1
}

# 回滾JAR
rollback_jar() {
    local module="$1"
    local backup_name="${2:-latest}"
    
    local backup_dir="${PROJECT_ROOT}/${BACKUP_DIR:-tools/backups}"
    local target_jar
    target_jar=$(get_server_plugin_path "$module")
    
    local backup_file
    if [[ "$backup_name" == "latest" ]]; then
        # 找最新的備份
        backup_file=$(find "$backup_dir" -name "${module}_*.jar" -type f | sort -r | head -n1)
    else
        backup_file="$backup_dir/$backup_name"
    fi
    
    if [[ ! -f "$backup_file" ]]; then
        error "找不到備份檔案: $backup_file"
    fi
    
    # 備份當前版本
    backup_existing_jar "$target_jar" "${module}_rollback"
    
    # 恢復備份版本
    cp "$backup_file" "$target_jar"
    
    success "模組 $module 已回滾到: $(basename "$backup_file")"
}

# 列出備份檔案
list_backups() {
    local module="${1:-all}"
    local backup_dir="${PROJECT_ROOT}/${BACKUP_DIR:-tools/backups}"
    
    if [[ ! -d "$backup_dir" ]]; then
        warning "備份目錄不存在: $backup_dir"
        return
    fi
    
    echo -e "${CYAN}📦 可用的備份檔案:${NC}"
    echo ""
    
    local pattern="*.jar"
    if [[ "$module" != "all" ]]; then
        pattern="${module}_*.jar"
    fi
    
    find "$backup_dir" -name "$pattern" -type f | sort -r | while read -r backup; do
        local basename_file
        basename_file=$(basename "$backup")
        local size
        size=$(du -h "$backup" | cut -f1)
        local date
        date=$(date -r "$backup" '+%Y-%m-%d %H:%M:%S')
        
        echo -e "  📄 ${GREEN}$basename_file${NC} (${size}, $date)"
    done
    
    echo ""
}

# 清理部署相關檔案
cleanup_deploy() {
    local target="${1:-all}"
    
    local plugins_dir="${PROJECT_ROOT}/${PLUGINS_RELATIVE_PATH:-minecraft-server/plugins}"
    
    if [[ "$target" == "all" ]]; then
        # 清理所有部署資訊檔案
        find "$plugins_dir" -name ".*.deploy-info" -delete
        find "$plugins_dir" -name ".*.deployed" -delete
        success "已清理所有部署資訊檔案"
    else
        # 清理特定模組
        rm -f "$plugins_dir/.$target.deploy-info"
        rm -f "$plugins_dir/.$target.deployed"
        success "已清理模組 $target 的部署資訊檔案"
    fi
}

# 顯示部署狀態
show_deploy_status() {
    local module="${1:-all}"
    
    local plugins_dir="${PROJECT_ROOT}/${PLUGINS_RELATIVE_PATH:-minecraft-server/plugins}"
    
    echo -e "${CYAN}🚀 部署狀態:${NC}"
    echo ""
    
    for mod in $MODULES; do
        if [[ "$module" != "all" && "$mod" != "$module" ]]; then
            continue
        fi
        
        local jar_path
        jar_path=$(get_server_plugin_path "$mod")
        local deploy_info="$plugins_dir/.$mod.deploy-info"
        
        echo -e "${YELLOW}模組: $mod${NC}"
        
        if [[ -f "$jar_path" ]]; then
            local size
            size=$(du -h "$jar_path" | cut -f1)
            local date
            date=$(date -r "$jar_path" '+%Y-%m-%d %H:%M:%S')
            echo -e "  ✅ JAR: 已部署 (${size}, $date)"
            
            if [[ -f "$deploy_info" ]]; then
                while IFS='=' read -r key value; do
                    case "$key" in
                        DEPLOY_TIME) echo -e "  📅 部署時間: $value" ;;
                        VERSION) echo -e "  🏷️  版本: $value" ;;
                        DEPLOYED_BY) echo -e "  👤 部署者: $value" ;;
                    esac
                done < "$deploy_info"
            fi
        else
            echo -e "  ❌ JAR: 未部署"
        fi
        
        echo ""
    done
}

# 顯示幫助
show_help() {
    cat << 'EOF'
🚀 Zientis JAR 部署輔助工具

用法: ./deploy-helper.sh [命令] [選項]

命令:
  deploy <module>              部署指定模組的JAR
  deploy-all                   部署所有模組的JAR
  rollback <module> [backup]   回滾模組到指定備份
  list-backups [module]        列出備份檔案
  status [module]              顯示部署狀態
  cleanup [module]             清理部署資訊檔案
  hot-reload <module>          手動觸發熱重載

選項:
  -f, --force                  強制部署（忽略雜湊值檢查）
  -h, --help                   顯示此幫助訊息

範例:
  ./deploy-helper.sh deploy core              # 部署核心模組
  ./deploy-helper.sh deploy-all               # 部署所有模組
  ./deploy-helper.sh rollback core            # 回滾核心模組到最新備份
  ./deploy-helper.sh list-backups             # 列出所有備份
  ./deploy-helper.sh status                   # 顯示所有模組部署狀態
  ./deploy-helper.sh cleanup                  # 清理所有部署資訊

EOF
}

# 主函數
main() {
    local command="${1:-help}"
    local force="false"
    
    # 解析參數
    while [[ $# -gt 0 ]]; do
        case $1 in
            -f|--force)
                force="true"
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            deploy)
                command="deploy"
                shift
                break
                ;;
            deploy-all)
                command="deploy-all"
                shift
                break
                ;;
            rollback)
                command="rollback"
                shift
                break
                ;;
            list-backups)
                command="list-backups"
                shift
                break
                ;;
            status)
                command="status"
                shift
                break
                ;;
            cleanup)
                command="cleanup"
                shift
                break
                ;;
            hot-reload)
                command="hot-reload"
                shift
                break
                ;;
            help)
                show_help
                exit 0
                ;;
            *)
                break
                ;;
        esac
    done
    
    case "$command" in
        deploy)
            local module="$1"
            if [[ -z "$module" ]]; then
                error "請指定要部署的模組"
            fi
            deploy_jar "$module" "$force"
            ;;
        deploy-all)
            for module in $MODULES; do
                deploy_jar "$module" "$force"
            done
            ;;
        rollback)
            local module="$1"
            local backup="${2:-latest}"
            if [[ -z "$module" ]]; then
                error "請指定要回滾的模組"
            fi
            rollback_jar "$module" "$backup"
            ;;
        list-backups)
            local module="${1:-all}"
            list_backups "$module"
            ;;
        status)
            local module="${1:-all}"
            show_deploy_status "$module"
            ;;
        cleanup)
            local target="${1:-all}"
            cleanup_deploy "$target"
            ;;
        hot-reload)
            local module="$1"
            if [[ -z "$module" ]]; then
                error "請指定要熱重載的模組"
            fi
            attempt_hot_reload "$module"
            ;;
        *)
            show_help
            exit 1
            ;;
    esac
}

# 執行主函數
main "$@"