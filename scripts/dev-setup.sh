#!/bin/bash
# Zientis 開發環境設置腳本

set -e

echo "🚀 Zientis 開發環境設置開始..."

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 檢查 Docker 是否安裝
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker 未安裝，請先安裝 Docker${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose 未安裝，請先安裝 Docker Compose${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker 環境檢查通過${NC}"

# 啟動開發服務
echo -e "${BLUE}📦 啟動開發環境服務...${NC}"
docker-compose -f docker-compose.dev.yml up -d

# 等待資料庫啟動
echo -e "${YELLOW}⏳ 等待 MariaDB 啟動...${NC}"
sleep 15

# 檢查服務狀態
echo -e "${BLUE}🔍 檢查服務狀態...${NC}"
docker-compose -f docker-compose.dev.yml ps

# 測試資料庫連線
echo -e "${BLUE}🔗 測試資料庫連線...${NC}"
if docker-compose -f docker-compose.dev.yml exec -T mariadb mysql -uzientis_dev -pdev_password_2024 -e "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ 資料庫連線成功${NC}"
else
    echo -e "${RED}❌ 資料庫連線失敗${NC}"
    exit 1
fi

# 建置專案
echo -e "${BLUE}🔨 建置專案...${NC}"
./gradlew clean build -x test

echo -e "${GREEN}🎉 開發環境設置完成！${NC}"
echo ""
echo -e "${BLUE}📋 服務資訊：${NC}"
echo -e "  📊 phpMyAdmin: http://localhost:8080"
echo -e "  🗄️  MariaDB: localhost:3306"
echo -e "  📦 Redis: localhost:6379"
echo ""
echo -e "${BLUE}🛠️  常用指令：${NC}"
echo -e "  停止服務: docker-compose -f docker-compose.dev.yml down"
echo -e "  查看日誌: docker-compose -f docker-compose.dev.yml logs -f"
echo -e "  重啟服務: docker-compose -f docker-compose.dev.yml restart"
echo ""
echo -e "${YELLOW}💡 下一步：${NC}"
echo -e "  1. 修改 config/development.yml 配置"
echo -e "  2. 執行 ./scripts/test-server.sh 啟動測試伺服器"
echo -e "  3. 訪問 http://localhost:8080 管理資料庫"