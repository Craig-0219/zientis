#!/bin/bash

# =======================================================
# Zientis 自動編譯工具示範腳本
# =======================================================
# 展示所有工具功能的完整示範
# =======================================================

set -e

# 顏色輸出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${PURPLE}"
cat << 'EOF'
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║    🚀 Zientis 自動編譯工具套件 - 功能示範                 ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

pause() {
    echo -e "${YELLOW}按 Enter 繼續...${NC}"
    read -r
}

echo -e "${CYAN}📋 示範內容：${NC}"
echo "1. 查看工具幫助"
echo "2. 檢查部署狀態"
echo "3. 編譯核心模組"
echo "4. 部署模組"
echo "5. 備份管理"
echo "6. 快速編譯示範"
echo ""
pause

echo -e "${BLUE}====== 1. 查看自動編譯工具幫助 ======${NC}"
./auto-build.sh --help
pause

echo -e "${BLUE}====== 2. 查看部署狀態 ======${NC}"
./deploy-helper.sh status
pause

echo -e "${BLUE}====== 3. 乾跑模式編譯示範 ======${NC}"
echo -e "${CYAN}執行命令: ./auto-build.sh --dry-run core${NC}"
./auto-build.sh --dry-run core
pause

echo -e "${BLUE}====== 4. 實際編譯核心模組（快速模式）======${NC}"
echo -e "${CYAN}執行命令: ./auto-build.sh -q core${NC}"
./auto-build.sh -q zientis-core
pause

echo -e "${BLUE}====== 5. 檢查編譯後的JAR檔案 ======${NC}"
ls -la zientis-core/build/libs/
pause

echo -e "${BLUE}====== 6. 部署JAR到伺服器 ======${NC}"
echo -e "${CYAN}執行命令: ./deploy-helper.sh deploy zientis-core${NC}"
./deploy-helper.sh deploy zientis-core
pause

echo -e "${BLUE}====== 7. 檢查部署後狀態 ======${NC}"
./deploy-helper.sh status zientis-core
pause

echo -e "${BLUE}====== 8. 列出備份檔案 ======${NC}"
./deploy-helper.sh list-backups
pause

echo -e "${BLUE}====== 9. 檢查伺服器插件目錄 ======${NC}"
ls -la minecraft-server/plugins/zientis-*
pause

echo -e "${GREEN}"
cat << 'EOF'
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║  🎉 示範完成！工具已成功：                                 ║
║                                                          ║
║  ✅ 智慧編譯檢測                                         ║
║  ✅ 自動JAR部署                                          ║ 
║  ✅ 版本管理                                            ║
║  ✅ 備份機制                                            ║
║  ✅ 狀態監控                                            ║
║                                                          ║
║  🚀 開發效率大幅提升！                                    ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

echo -e "${CYAN}💡 下一步建議：${NC}"
echo "• 使用 ./auto-build.sh -w 啟動監控模式進行開發"
echo "• 配置 build.conf 以符合你的需求"
echo "• 設定通知系統以獲得編譯結果提醒"
echo ""

echo -e "${BLUE}📚 更多資訊請查看：${NC}"
echo "• tools/README.md - 完整使用指南"
echo "• tools/build.conf - 配置選項說明"
echo ""