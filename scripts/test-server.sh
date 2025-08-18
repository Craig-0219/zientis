#!/bin/bash
# Zientis 測試伺服器啟動腳本

set -e

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🎮 Zientis 測試伺服器啟動...${NC}"

# 建置最新版本
echo -e "${BLUE}🔨 建置最新插件...${NC}"
./gradlew :zientis-economy:build :zientis-multiworld:build --continue

# 檢查建置是否成功
if [ ! -f "zientis-economy/build/libs/zientis-economy-0.1.0-ALPHA.jar" ]; then
    echo -e "${RED}❌ Economy 插件建置失敗${NC}"
    exit 1
fi

if [ ! -f "zientis-multiworld/build/libs/zientis-multiworld-0.1.0-ALPHA.jar" ]; then
    echo -e "${RED}❌ MultiWorld 插件建置失敗${NC}"
    exit 1
fi

# 複製插件到測試伺服器
echo -e "${BLUE}📦 複製插件到測試伺服器...${NC}"
cp zientis-economy/build/libs/zientis-economy-0.1.0-ALPHA.jar minecraft-server/plugins/
cp zientis-multiworld/build/libs/zientis-multiworld-0.1.0-ALPHA.jar minecraft-server/plugins/

echo -e "${GREEN}✅ 插件複製完成${NC}"

# 切換到伺服器目錄
cd minecraft-server

# 檢查是否有Paper伺服器
if [ ! -f "paper-1.20.6.jar" ]; then
    echo -e "${YELLOW}⬇️  Paper伺服器不存在，正在下載...${NC}"
    wget -O paper-1.20.6.jar https://api.papermc.io/v2/projects/paper/versions/1.20.6/builds/147/downloads/paper-1.20.6-147.jar
fi

# 啟動伺服器
echo -e "${GREEN}🚀 啟動 Paper 測試伺服器...${NC}"
echo -e "${YELLOW}⚠️  使用 Ctrl+C 停止伺服器${NC}"
echo ""

java -Xmx2G -Xms1G -jar paper-1.20.6.jar --nogui