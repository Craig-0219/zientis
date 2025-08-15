# 🚀 賽恩堤斯伺服器部署指南

## 📋 目錄
- [系統需求](#系統需求)
- [依賴軟體安裝](#依賴軟體安裝)
- [專案構建](#專案構建)
- [伺服器設置](#伺服器設置)
- [插件安裝](#插件安裝)
- [資料庫配置](#資料庫配置)
- [配置文件](#配置文件)
- [啟動伺服器](#啟動伺服器)
- [測試驗證](#測試驗證)
- [維護操作](#維護操作)
- [故障排除](#故障排除)

---

## 🖥️ 系統需求

### 最低需求
- **作業系統**: Linux (Ubuntu 20.04+, CentOS 8+) / Windows Server 2019+
- **RAM**: 8GB (推薦16GB+)
- **CPU**: 4核心 (推薦8核心+)
- **儲存空間**: 100GB SSD (推薦500GB+)
- **網路**: 100Mbps上傳 (推薦1Gbps)

### 推薦配置
- **作業系統**: Ubuntu 22.04 LTS
- **RAM**: 32GB
- **CPU**: 16核心 (Intel Xeon / AMD EPYC)
- **儲存空間**: 1TB NVMe SSD
- **網路**: 1Gbps 專線

---

## 📦 依賴軟體安裝

### 1. Java 21 安裝

#### Ubuntu/Debian
```bash
# 更新套件列表
sudo apt update

# 安裝 OpenJDK 21
sudo apt install openjdk-21-jdk

# 驗證安裝
java -version
javac -version
```

#### CentOS/RHEL
```bash
# 安裝 OpenJDK 21
sudo dnf install java-21-openjdk java-21-openjdk-devel

# 驗證安裝
java -version
javac -version
```

### 2. MariaDB 安裝與配置

#### Ubuntu/Debian
```bash
# 安裝 MariaDB
sudo apt install mariadb-server mariadb-client

# 啟動並設置開機自啟
sudo systemctl start mariadb
sudo systemctl enable mariadb

# 安全配置
sudo mysql_secure_installation
```

#### 資料庫初始化
```sql
-- 登入 MariaDB
sudo mysql -u root -p

-- 創建資料庫
CREATE DATABASE zientis_main CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE zientis_economy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 創建用戶並授予權限
CREATE USER 'zientis'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON zientis_main.* TO 'zientis'@'localhost';
GRANT ALL PRIVILEGES ON zientis_economy.* TO 'zientis'@'localhost';
FLUSH PRIVILEGES;

-- 驗證
SHOW DATABASES;
EXIT;
```

### 3. Redis 安裝與配置

#### Ubuntu/Debian
```bash
# 安裝 Redis
sudo apt install redis-server

# 配置 Redis
sudo nano /etc/redis/redis.conf

# 修改以下設置：
# maxmemory 2gb
# maxmemory-policy allkeys-lru
# save 900 1
# save 300 10

# 重啟 Redis
sudo systemctl restart redis-server
sudo systemctl enable redis-server

# 測試連接
redis-cli ping
```

---

## 🔨 專案構建

### 1. 取得原始碼
```bash
# 克隆專案
git clone https://github.com/your-repo/zientis-server.git
cd zientis-server

# 檢查分支
git branch -a
git checkout main
```

### 2. Gradle 構建
```bash
# 確保 Gradle Wrapper 可執行
chmod +x gradlew

# 清理並構建
./gradlew clean

# 運行測試
./gradlew test

# 構建所有模組
./gradlew build

# 生成JAR檔案
./gradlew jar
```

### 3. 驗證構建結果
```bash
# 檢查生成的JAR檔案
find . -name "*.jar" -type f

# 應該看到以下檔案：
# ./zientis-core/build/libs/zientis-core-1.0.0.jar
# ./zientis-multiworld/build/libs/zientis-multiworld-1.0.0.jar
# ./zientis-economy/build/libs/zientis-economy-1.0.0.jar
# ./zientis-display/build/libs/zientis-display-1.0.0.jar
# ./zientis-nations/build/libs/zientis-nations-1.0.0.jar
# ./zientis-social/build/libs/zientis-social-1.0.0.jar
```

---

## 🌐 伺服器設置

### 1. 創建伺服器目錄
```bash
# 創建主要目錄
sudo mkdir -p /opt/minecraft/zientis
cd /opt/minecraft/zientis

# 設置權限
sudo chown -R minecraft:minecraft /opt/minecraft/zientis
```

### 2. 下載 Paper 伺服器
```bash
# 下載 Paper 1.20.6
wget https://api.papermc.io/v2/projects/paper/versions/1.20.6/builds/latest/downloads/paper-1.20.6-XXX.jar -O paper.jar

# 建立啟動腳本
cat > start.sh << 'EOF'
#!/bin/bash
java -Xms8G -Xmx16G \
     -XX:+UseG1GC \
     -XX:+ParallelRefProcEnabled \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UnlockExperimentalVMOptions \
     -XX:+DisableExplicitGC \
     -XX:+AlwaysPreTouch \
     -XX:G1NewSizePercent=30 \
     -XX:G1MaxNewSizePercent=40 \
     -XX:G1HeapRegionSize=8M \
     -XX:G1ReservePercent=20 \
     -XX:G1HeapWastePercent=5 \
     -XX:G1MixedGCCountTarget=4 \
     -XX:InitiatingHeapOccupancyPercent=15 \
     -XX:G1MixedGCLiveThresholdPercent=90 \
     -XX:G1RSetUpdatingPauseTimePercent=5 \
     -XX:SurvivorRatio=32 \
     -XX:+PerfDisableSharedMem \
     -XX:MaxTenuringThreshold=1 \
     -Dusing.aikars.flags=https://mcflags.emc.gs \
     -Daikars.new.flags=true \
     -jar paper.jar nogui
EOF

chmod +x start.sh
```

### 3. 初始伺服器設置
```bash
# 首次啟動（生成檔案）
./start.sh

# 同意 EULA
echo "eula=true" > eula.txt

# 再次啟動
./start.sh
```

---

## 🔌 插件安裝

### 1. 必要依賴插件
```bash
# 進入 plugins 目錄
cd plugins/

# 下載 Vault
wget https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar

# 下載 BentoBox
wget https://github.com/BentoBoxWorld/BentoBox/releases/download/1.20.1/BentoBox-1.20.1.jar

# 下載 Slimefun4 (如需要)
# wget https://github.com/Slimefun/Slimefun4/releases/download/RC-32/Slimefun4-RC-32.jar
```

### 2. 安裝 Zientis 插件
```bash
# 複製構建的插件檔案
cp /path/to/zientis-server/zientis-core/build/libs/zientis-core-1.0.0.jar ./
cp /path/to/zientis-server/zientis-multiworld/build/libs/zientis-multiworld-1.0.0.jar ./
cp /path/to/zientis-server/zientis-economy/build/libs/zientis-economy-1.0.0.jar ./
# 注意：其他模組尚未完成，暫時不安裝

# 驗證插件檔案
ls -la *.jar
```

### 3. 插件載入順序設置
```yaml
# 創建 plugins/PluginMetrics/config.yml
enabled: true
server-uuid: generated-uuid-here
```

---

## 🔧 配置文件

### 1. 伺服器基本配置 (server.properties)
```properties
# 基本設置
server-name=Zientis Server
server-port=25565
online-mode=true
max-players=200

# 世界設置
level-name=world
level-type=flat
spawn-protection=0
allow-nether=true
allow-end=true

# 性能設置
view-distance=10
simulation-distance=8
entity-broadcast-range-percentage=100

# 其他設置
difficulty=normal
gamemode=survival
pvp=true
```

### 2. Paper 配置 (paper-global.yml)
```yaml
# 性能優化
chunk-loading:
  min-load-radius: 2
  max-concurrent-sends: 2
  autoconfig-send-distance: true
  target-player-chunk-send-rate: 100.0

async-chunks:
  enable: true
  threads: -1

# 實體優化
entities:
  spawning:
    all-chunks-are-slime-chunks: false
    iron-golems-can-spawn-in-air: false
  behavior:
    baby-zombie-movement-modifier: 0.5
    disable-chest-cat-detection: false
```

### 3. Zientis 經濟系統配置
```yaml
# plugins/ZientisEconomy/config.yml
database:
  type: mariadb
  host: localhost
  port: 3306
  database: zientis_economy
  username: zientis
  password: your_secure_password
  
economy:
  currency-name: "銀幣"
  currency-symbol: "§6⛀"
  starting-balance: 1000.0
  max-balance: 1000000000.0
  decimal-places: 2

features:
  transaction-logging: true
  account-freezing: true
  backup-enabled: true
  backup-interval: 3600  # 1小時
```

### 4. Zientis 多世界系統配置
```yaml
# plugins/ZientisMultiWorld/config.yml
world-management:
  template-world: "island_template"
  auto-unload-delay: 300  # 5分鐘無人則卸載
  max-loaded-worlds: 50
  backup-enabled: true
  backup-interval: 1800  # 30分鐘

memory-management:
  enable-optimization: true
  gc-frequency: 600  # 10分鐘
  memory-threshold: 85  # 85%記憶體使用率警告
```

---

## ▶️ 啟動伺服器

### 1. 系統服務設置 (推薦)
```bash
# 創建系統服務
sudo tee /etc/systemd/system/zientis.service << 'EOF'
[Unit]
Description=Zientis Minecraft Server
After=network.target

[Service]
Type=simple
User=minecraft
Group=minecraft
WorkingDirectory=/opt/minecraft/zientis
ExecStart=/opt/minecraft/zientis/start.sh
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 啟用並啟動服務
sudo systemctl daemon-reload
sudo systemctl enable zientis
sudo systemctl start zientis

# 檢查狀態
sudo systemctl status zientis
```

### 2. 監控日誌
```bash
# 即時監控伺服器日誌
sudo journalctl -f -u zientis

# 或直接查看檔案
tail -f /opt/minecraft/zientis/logs/latest.log
```

---

## ✅ 測試驗證

### 1. 基本功能測試
```bash
# 連接到伺服器控制台
sudo systemctl status zientis
# 或使用 screen/tmux 連接

# 在遊戲中測試指令：
# /balance - 檢查餘額
# /pay <player> <amount> - 轉帳測試
# /economy stats - 查看經濟統計
```

### 2. 性能測試
```bash
# 監控伺服器資源使用
htop
iostat -x 1
free -h

# 檢查 TPS
# 在遊戲中執行 /tps
```

### 3. 資料庫連接測試
```bash
# 測試 MariaDB 連接
mysql -u zientis -p zientis_economy -e "SHOW TABLES;"

# 測試 Redis 連接
redis-cli ping
```

---

## 🔧 維護操作

### 1. 定期備份
```bash
# 創建備份腳本
cat > /opt/minecraft/backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/minecraft/backups/$DATE"

# 創建備份目錄
mkdir -p "$BACKUP_DIR"

# 備份世界檔案
cp -r /opt/minecraft/zientis/world* "$BACKUP_DIR/"

# 備份插件配置
cp -r /opt/minecraft/zientis/plugins/ "$BACKUP_DIR/"

# 備份資料庫
mysqldump -u zientis -p zientis_main > "$BACKUP_DIR/database_main.sql"
mysqldump -u zientis -p zientis_economy > "$BACKUP_DIR/database_economy.sql"

# 壓縮備份
tar -czf "$BACKUP_DIR.tar.gz" -C /opt/minecraft/backups "$DATE"
rm -rf "$BACKUP_DIR"

# 清理舊備份（保留7天）
find /opt/minecraft/backups -name "*.tar.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_DIR.tar.gz"
EOF

chmod +x /opt/minecraft/backup.sh

# 設置定時備份
crontab -e
# 添加：0 2 * * * /opt/minecraft/backup.sh
```

### 2. 更新插件
```bash
# 停止伺服器
sudo systemctl stop zientis

# 備份舊版本
cp plugins/zientis-*.jar backups/

# 複製新版本
cp new-version/*.jar plugins/

# 重啟伺服器
sudo systemctl start zientis
```

### 3. 性能優化監控
```bash
# 檢查記憶體使用
free -h

# 檢查磁碟 I/O
iotop

# 檢查網路使用
iftop
```

---

## 🚨 故障排除

### 1. 常見問題

#### 伺服器無法啟動
```bash
# 檢查Java版本
java -version

# 檢查記憶體
free -h

# 檢查日誌
tail -f logs/latest.log
```

#### 資料庫連接失敗
```bash
# 檢查 MariaDB 狀態
sudo systemctl status mariadb

# 測試連接
mysql -u zientis -p -e "SHOW DATABASES;"

# 檢查防火牆
sudo ufw status
```

#### 插件載入失敗
```bash
# 檢查插件相依性
ls -la plugins/

# 檢查 Java 版本相容性
java -version

# 查看插件錯誤
grep -i error logs/latest.log
```

### 2. 性能問題診斷

#### TPS 過低
- 檢查實體數量
- 優化區塊載入
- 檢查插件衝突
- 調整 JVM 參數

#### 記憶體洩漏
- 監控 Java heap 使用
- 檢查世界卸載機制
- 分析 GC 日誌
- 重啟伺服器

### 3. 聯絡支援
如遇到無法解決的問題：
- **GitHub Issues**: https://github.com/your-repo/zientis-server/issues
- **Discord**: 邀請碼 UxpV7Yr9V8
- **Email**: craig900219@gmail.com

---

## 📝 附錄

### A. 推薦硬體配置表

| 玩家數量 | RAM | CPU | 儲存空間 | 網路頻寬 |
|---------|-----|-----|----------|----------|
| 20-50   | 8GB | 4核心 | 200GB SSD | 100Mbps |
| 50-100  | 16GB | 8核心 | 500GB SSD | 500Mbps |
| 100-200 | 32GB | 16核心 | 1TB SSD | 1Gbps |
| 200+    | 64GB | 32核心 | 2TB SSD | 10Gbps |

### B. 網路埠設置

| 服務 | 埠號 | 協議 | 說明 |
|------|------|------|------|
| Minecraft | 25565 | TCP | 主要遊戲埠 |
| MariaDB | 3306 | TCP | 資料庫（內部） |
| Redis | 6379 | TCP | 快取（內部） |
| SSH | 22 | TCP | 遠端管理 |

### C. 安全建議
- 定期更新系統和軟體
- 使用強密碼和金鑰認證
- 設置防火牆規則
- 定期備份重要資料
- 監控系統日誌

---

*最後更新：2024年12月15日*
*版本：1.0.0*