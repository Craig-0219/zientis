# 🛠️ Zientis 開發工具套件

這個目錄包含了 Zientis 專案的自動化開發工具，讓開發、編譯、部署變得更加簡單高效。

## 🚀 新功能：統一 CLI 工具

現在所有開發工具都已整合到單一 CLI 介面中！支援 **Windows、Linux、macOS** 跨平台使用。

### 🪟 Windows 用戶

```cmd
REM 從專案根目錄使用
zientis.bat build all

REM 或從 tools 目錄使用
tools\zientis-cli.bat build all

REM 查看幫助
zientis.bat help
```

### 🐧 Linux/macOS 用戶

```bash
# 從專案根目錄使用（推薦）
./zientis build all

# 建立全域命令鏈結（可選）
sudo ln -sf $(pwd)/tools/zientis-cli /usr/local/bin/zientis
zientis build all

# 查看幫助
./zientis help
```

### 📦 自動平台檢測

專案根目錄的 `zientis` 和 `zientis.bat` 會自動檢測您的作業系統並選擇正確的 CLI 版本。

## 🚀 工具清單

### 1. 自動編譯工具 (`build-runner.main.kts`)
智慧型 Gradle 編譯工具，支援多模組專案的自動化編譯與部署。

**主要功能**:
- 🎯 智慧變更檢測，只編譯有修改的模組
- ⚡ 並行編譯，提升編譯速度
- 👀 監控模式，檔案變更時自動重新編譯
- 🔄 自動複製 JAR 到 Minecraft 伺服器
- 🎨 彩色輸出，清晰的進度顯示
- 📋 詳細的編譯報告和錯誤處理

### 2. 部署輔助工具 (`deploy-runner.main.kts`)
專業級的 JAR 部署管理工具，提供完整的版本控制和回滾功能。

**主要功能**:
- 📦 智慧 JAR 部署與備份
- 🔄 熱重載支援
- ⏪ 版本回滾功能
- 📊 部署狀態監控
- 🛡️ 檔案完整性校驗（SHA256）
- 🗂️ 自動備份管理

### 3. 配置檔案 (`build.conf`)
統一的配置管理，支援所有工具的個性化設定。

**主要配置**:
- 📝 模組清單與依賴關係
- ⚙️ 編譯參數調優
- 🚀 部署自動化設定
- 📺 監控與通知配置
- 🔒 安全與備份設定

## 🎯 快速開始

### 使用統一 CLI（推薦）

#### Windows 用戶
```cmd
REM 編譯所有模組
zientis.bat build all

REM 編譯特定模組
zientis.bat build core

REM 監控模式（開發時推薦）
zientis.bat build watch

REM 部署管理
zientis.bat deploy status
zientis.bat deploy all
zientis.bat deploy rollback core

REM 查看所有可用命令
zientis.bat help
```

#### Linux/macOS 用戶
```bash
# 編譯所有模組
./zientis build all

# 編譯特定模組
./zientis build core

# 監控模式（開發時推薦）
./zientis build watch

# 部署管理
./zientis deploy status
./zientis deploy all
./zientis deploy rollback core

# 查看所有可用命令
./zientis help
```

### 傳統方式（仍然支援）

#### 安裝依賴
```bash
# 確保已安裝必要工具
sudo apt update
sudo apt install -y openjdk-21-jdk gradle git rsync kotlin
```

#### 初始化配置
```bash
# 進入工具目錄
cd tools

# 首次執行會自動創建配置檔案
kotlin -s ./build-runner.main.kts --help
```

#### 直接執行腳本
```bash
# 編譯所有模組
kotlin -s ./build-runner.main.kts -a

# 編譯特定模組  
kotlin -s ./build-runner.main.kts core economy

# 監控模式
kotlin -s ./build-runner.main.kts -w

# 部署管理
kotlin -s ./deploy-runner.main.kts status
kotlin -s ./deploy-runner.main.kts deploy-all
kotlin -s ./deploy-runner.main.kts rollback core
```

## 📋 詳細使用指南

### CLI 命令結構

```bash
zientis <command> [subcommand] [options] [arguments]
```

### 編譯命令 (`zientis build`)

| 子命令 | 說明 | 範例 |
|--------|------|------|
| `all` | 編譯所有模組 | `zientis build all` |
| `<module>` | 編譯指定模組 | `zientis build core` |
| `watch` | 監控模式，自動重新編譯 | `zientis build watch` |
| `clean` | 清理編譯快取 | `zientis build clean` |

**編譯選項**:
- `-c, --clean` - 編譯前先清理
- `-q, --quick` - 快速模式（跳過測試）
- `-f, --force` - 強制重新編譯
- `-r, --restart` - 編譯後重啟伺服器
- `-v, --verbose` - 詳細輸出
- `--dry-run` - 乾跑模式

### 部署命令 (`zientis deploy`)

| 子命令 | 說明 | 範例 |
|--------|------|------|
| `status` | 查看部署狀態 | `zientis deploy status` |
| `<module>` | 部署指定模組 | `zientis deploy core` |
| `all` | 部署所有模組 | `zientis deploy all` |
| `rollback <module>` | 回滾指定模組 | `zientis deploy rollback core` |
| `list-backups` | 列出所有備份 | `zientis deploy list-backups` |
| `cleanup` | 清理舊備份 | `zientis deploy cleanup` |

### 傳統腳本選項（向下相容）

| 選項 | 說明 | 範例 |
|------|------|------|
| `-a, --all` | 編譯所有模組 | `kotlin -s ./build-runner.main.kts -a` |
| `-c, --clean` | 編譯前先清理 | `kotlin -s ./build-runner.main.kts -c core` |
| `-w, --watch` | 監控模式 | `kotlin -s ./build-runner.main.kts -w` |
| `-q, --quick` | 快速模式（跳過測試） | `kotlin -s ./build-runner.main.kts -q` |
| `-r, --restart` | 編譯後重啟伺服器 | `kotlin -s ./build-runner.main.kts -r` |
| `-f, --force` | 強制重新編譯 | `kotlin -s ./build-runner.main.kts -f` |
| `-v, --verbose` | 詳細輸出 | `kotlin -s ./build-runner.main.kts -v` |
| `--dry-run` | 乾跑模式 | `kotlin -s ./build-runner.main.kts --dry-run` |

### 模組別名

| 別名 | 完整模組名 | 說明 |
|------|-----------|------|
| `core` | `zientis-core` | 核心模組 |
| `economy` | `zientis-economy` | 經濟系統 |
| `multiworld` | `zientis-multiworld` | 多世界管理 |
| `social` | `zientis-social` | 社交系統 |
| `nations` | `zientis-nations` | 國家系統 |
| `display` | `zientis-display` | 顯示介面 |
| `discord-api` | `zientis-discord-api` | Discord API |

### 部署工具命令

| 命令 | 說明 | 範例 |
|------|------|------|
| `deploy <module>` | 部署指定模組 | `kotlin -s ./deploy-runner.main.kts deploy core` |
| `deploy-all` | 部署所有模組 | `kotlin -s ./deploy-runner.main.kts deploy-all` |
| `rollback <module>` | 回滾模組 | `kotlin -s ./deploy-runner.main.kts rollback core` |
| `list-backups` | 列出備份 | `kotlin -s ./deploy-runner.main.kts list-backups` |
| `status` | 顯示部署狀態 | `kotlin -s ./deploy-runner.main.kts status` |
| `cleanup` | 清理部署檔案 | `kotlin -s ./deploy-runner.main.kts cleanup` |

## 🔧 高級配置

### 自訂編譯流程

在 `build.conf` 中可以設定：

```bash
# 自訂 Gradle 參數
CUSTOM_GRADLE_ARGS="--console=plain --parallel"

# 預編譯 Hook
PRE_BUILD_HOOK="/path/to/pre-build-script.sh"

# 後編譯 Hook  
POST_BUILD_HOOK="/path/to/post-build-script.sh"
```

### 熱重載設定

```bash
# 支援熱重載的模組
HOT_RELOAD_MODULES="zientis-core"

# 熱重載延遲時間
HOT_RELOAD_DELAY=3
```

### 通知系統

```bash
# 啟用通知
ENABLE_NOTIFICATIONS=true

# Discord Webhook
DISCORD_WEBHOOK_URL="https://discord.com/api/webhooks/..."

# 電子郵件通知
EMAIL_NOTIFICATIONS=true
SMTP_SERVER="smtp.gmail.com"
```

## 🎨 開發工作流程

### 推薦的開發流程（使用 CLI）

#### Windows 用戶

1. **啟動監控模式**
   ```cmd
   zientis.bat build watch
   ```

2. **問題調試**
   ```cmd
   REM 詳細編譯輸出
   zientis.bat build core --verbose
   
   REM 檢查部署狀態
   zientis.bat deploy status
   ```

3. **版本回滾**
   ```cmd
   REM 如果新版本有問題，快速回滾
   zientis.bat deploy rollback core
   ```

4. **團隊協作**
   ```cmd
   REM 強制重新編譯所有模組（同步最新變更）
   zientis.bat build all --force

   REM 清理並重新編譯（解決依賴問題）
   zientis.bat build clean
   ```

#### Linux/macOS 用戶

1. **啟動監控模式**
   ```bash
   ./zientis build watch
   ```

2. **問題調試**
   ```bash
   # 詳細編譯輸出
   ./zientis build core --verbose
   
   # 檢查部署狀態
   ./zientis deploy status
   ```

3. **版本回滾**
   ```bash
   # 如果新版本有問題，快速回滾
   ./zientis deploy rollback core
   ```

4. **團隊協作**
   ```bash
   # 強制重新編譯所有模組（同步最新變更）
   ./zientis build all --force

   # 清理並重新編譯（解決依賴問題）
   ./zientis build clean
   ```

### 通用流程

**編輯程式碼** → 工具會自動檢測檔案變更 → 自動編譯並部署到伺服器

**測試功能** → 可以即時在 Minecraft 伺服器中測試 → 支援熱重載的模組無需重啟伺服器

### 傳統流程（向下相容）

1. **啟動監控模式**
   ```bash
   kotlin -s ./build-runner.main.kts -w
   ```

2. **問題調試**
   ```bash
   # 詳細編譯輸出
   kotlin -s ./build-runner.main.kts -v core
   
   # 檢查部署狀態
   kotlin -s ./deploy-runner.main.kts status core
   ```

3. **版本回滾**
   ```bash
   # 如果新版本有問題，快速回滾
   kotlin -s ./deploy-runner.main.kts rollback core
   ```

4. **團隊協作**
   ```bash
   # 強制重新編譯所有模組（同步最新變更）
   kotlin -s ./build-runner.main.kts -f -a
   
   # 清理並重新編譯（解決依賴問題）
   kotlin -s ./build-runner.main.kts -c -a
   ```

## 🐛 故障排除

### 常見問題

**1. 編譯失敗**
```bash
# 檢查 Java 版本
java -version

# 檢查 Gradle 版本
gradle -version

# 清理並重新編譯
kotlin -s ./build-runner.main.kts -c -f core
```

**2. 部署失敗**
```bash
# 檢查檔案權限
ls -la minecraft-server/plugins/

# 檢查伺服器狀態
kotlin -s ./deploy-runner.main.kts status

# 手動部署
kotlin -s ./deploy-runner.main.kts deploy core -f
```

**3. 監控模式異常**
```bash
# 檢查監控間隔設定
grep WATCH_INTERVAL build.conf

# 降低監控頻率
echo "WATCH_INTERVAL=5" >> build.conf
```

### 日誌分析

工具會在以下位置生成日誌：
- `tools/logs/auto-build.log` - 編譯日誌
- `minecraft-server/plugins/.*.deploy-info` - 部署資訊
- `tools/backups/` - 備份檔案

## 🔄 更新說明

### v1.0 功能亮點
- ✨ 全新的監控模式
- 🚀 智慧依賴檢測
- 📦 自動備份管理
- 🔄 熱重載支援
- 📊 詳細狀態報告
- 🎨 美觀的彩色輸出

### CLI v1.0 新功能
- ✨ **統一介面** - 所有工具整合到單一 CLI
- 🔄 **智慧回退** - Kotlin 未安裝時自動使用 Shell 腳本
- 📋 **完整幫助** - 每個命令都有詳細說明
- 🌍 **全域可用** - 建立符號鏈結後任何地方都能使用
- 🎨 **友善介面** - 彩色輸出和清晰的錯誤訊息

### 下一版本計劃
- 🌐 遠端部署支援
- 📱 移動端推送通知  
- 🤖 AI 輔助錯誤診斷
- 📈 效能分析報告
- 🔐 加密部署支援

## 📞 技術支援

如遇問題，請：

1. **使用 CLI 查看詳細資訊**：

   **Windows**:
   ```cmd
   zientis.bat build core --verbose  REM 詳細編譯輸出
   zientis.bat deploy status         REM 部署狀態
   ```

   **Linux/macOS**:
   ```bash
   ./zientis build core --verbose    # 詳細編譯輸出
   ./zientis deploy status           # 部署狀態
   ```

2. **檢查配置檔案**：
   ```bash
   cat tools/build.conf
   ```

3. **確認依賴完整**：
   ```bash
   java -version && gradle -version
   ```

4. **傳統方式故障排除**：
   ```bash
   kotlin -s ./build-runner.main.kts -v
   ```

5. 提交 Issue 到專案儲存庫

---

**🎯 讓開發更簡單，讓部署更可靠！現在更統一更方便！**