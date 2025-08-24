#!/bin/bash

# =======================================================
# Zientis 自動編譯工具 - 快速設定腳本
# =======================================================
# 一鍵設定開發環境和工具配置
# =======================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 顏色輸出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"; }
success() { echo -e "${GREEN}✅ $1${NC}"; }
warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
error() { echo -e "${RED}❌ $1${NC}"; exit 1; }
info() { echo -e "${CYAN}ℹ️  $1${NC}"; }

echo -e "${PURPLE}"
cat << 'EOF'
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║        🛠️ Zientis 自動編譯工具 - 快速設定                 ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# 檢查系統需求
log "檢查系統依賴..."

check_command() {
    local cmd="$1"
    local package="$2"
    
    if command -v "$cmd" &> /dev/null; then
        success "$cmd 已安裝"
        return 0
    else
        warning "$cmd 未安裝"
        if [[ -n "$package" ]]; then
            info "可以使用以下命令安裝: sudo apt install $package"
        fi
        return 1
    fi
}

# 必需依賴
REQUIRED_DEPS=(
    "java:openjdk-21-jdk"
    "gradle:gradle"
    "git:git"
    "rsync:rsync"
    "sha256sum:coreutils"
)

missing_deps=()

for dep in "${REQUIRED_DEPS[@]}"; do
    IFS=':' read -r cmd package <<< "$dep"
    if ! check_command "$cmd" "$package"; then
        missing_deps+=("$package")
    fi
done

if [[ ${#missing_deps[@]} -gt 0 ]]; then
    error "缺少必要依賴，請安裝: ${missing_deps[*]}"
fi

# 檢查 Java 版本
java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [[ "$java_version" -lt 21 ]]; then
    warning "建議使用 Java 21 或更高版本，當前版本: $java_version"
fi

# 創建必要目錄
log "創建必要目錄..."

DIRS=(
    "$PROJECT_ROOT/tools/logs"
    "$PROJECT_ROOT/tools/backups"
    "$PROJECT_ROOT/minecraft-server/plugins"
)

for dir in "${DIRS[@]}"; do
    if [[ ! -d "$dir" ]]; then
        mkdir -p "$dir"
        success "創建目錄: $dir"
    else
        info "目錄已存在: $dir"
    fi
done

# 設定工具權限
log "設定工具執行權限..."

TOOLS=(
    "$SCRIPT_DIR/auto-build.sh"
    "$SCRIPT_DIR/deploy-helper.sh"
    "$SCRIPT_DIR/demo.sh"
    "$SCRIPT_DIR/setup.sh"
)

for tool in "${TOOLS[@]}"; do
    if [[ -f "$tool" ]]; then
        chmod +x "$tool"
        success "設定權限: $(basename "$tool")"
    else
        warning "工具不存在: $(basename "$tool")"
    fi
done

# 檢查配置檔案
log "檢查配置檔案..."

BUILD_CONFIG="$SCRIPT_DIR/build.conf"
if [[ -f "$BUILD_CONFIG" ]]; then
    success "配置檔案已存在: build.conf"
else
    warning "配置檔案不存在，將在首次執行時自動創建"
fi

# 測試 Gradle
log "測試 Gradle 配置..."

cd "$PROJECT_ROOT"
if ./gradlew tasks &> /dev/null; then
    success "Gradle 配置正常"
else
    warning "Gradle 配置可能有問題，請檢查"
fi

# 檢查現有 JAR 檔案
log "檢查現有 JAR 檔案..."

jar_count=0
for module_dir in zientis-*/; do
    if [[ -d "$module_dir/build/libs" ]]; then
        jar_files=$(find "$module_dir/build/libs" -name "*.jar" | wc -l)
        if [[ $jar_files -gt 0 ]]; then
            jar_count=$((jar_count + jar_files))
            success "找到 $jar_files 個 JAR 檔案在 $module_dir"
        fi
    fi
done

if [[ $jar_count -eq 0 ]]; then
    info "未找到現有 JAR 檔案，建議先執行編譯"
else
    success "總共找到 $jar_count 個 JAR 檔案"
fi

# 創建快捷方式腳本
log "創建快捷方式..."

cat > "$PROJECT_ROOT/build.sh" << 'EOF'
#!/bin/bash
# Zientis 快速編譯腳本
cd "$(dirname "$0")"
./tools/auto-build.sh "$@"
EOF

cat > "$PROJECT_ROOT/deploy.sh" << 'EOF'
#!/bin/bash
# Zientis 快速部署腳本  
cd "$(dirname "$0")"
./tools/deploy-helper.sh "$@"
EOF

chmod +x "$PROJECT_ROOT/build.sh" "$PROJECT_ROOT/deploy.sh"
success "創建快捷方式: build.sh, deploy.sh"

# 生成使用建議
echo ""
echo -e "${CYAN}🎯 設定完成！建議的下一步操作：${NC}"
echo ""

echo -e "${YELLOW}1. 編譯所有模組：${NC}"
echo "   ./build.sh -a"
echo ""

echo -e "${YELLOW}2. 啟動監控模式（開發推薦）：${NC}"
echo "   ./build.sh -w"
echo ""

echo -e "${YELLOW}3. 快速編譯核心模組：${NC}"
echo "   ./build.sh -q core"
echo ""

echo -e "${YELLOW}4. 部署模組到伺服器：${NC}"
echo "   ./deploy.sh deploy-all"
echo ""

echo -e "${YELLOW}5. 查看工具功能示範：${NC}"
echo "   ./tools/demo.sh"
echo ""

echo -e "${YELLOW}6. 查看詳細說明：${NC}"
echo "   cat tools/README.md"
echo ""

# 詢問是否執行示範
echo -e "${CYAN}是否要執行功能示範？(y/N): ${NC}"
read -r response
if [[ "$response" =~ ^[Yy]$ ]]; then
    echo ""
    log "啟動功能示範..."
    "$SCRIPT_DIR/demo.sh"
fi

echo ""
success "設定完成！開始享受高效的開發體驗吧！"
echo ""