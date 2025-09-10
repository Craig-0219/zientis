# 🚀 Zientis CLI 工具

統一的跨平台開發工具介面，整合編譯、部署、環境設定等所有開發功能。

## 📦 快速開始

### 🪟 Windows 用戶

```cmd
# 編譯所有模組
zientis.bat build all

# 編譯特定模組
zientis.bat build core

# 監控模式
zientis.bat build watch

# 查看幫助
zientis.bat help
```

### 🐧 Linux/macOS 用戶

```bash
# 編譯所有模組
./zientis build all

# 編譯特定模組
./zientis build core

# 監控模式
./zientis build watch

# 查看幫助
./zientis help
```

## 🛠️ 主要功能

### 編譯命令
- `build all` - 編譯所有模組
- `build <module>` - 編譯指定模組 (core, economy, social 等)
- `build watch` - 監控模式，自動重新編譯
- `build clean` - 清理編譯快取

### 部署命令
- `deploy status` - 查看部署狀態
- `deploy all` - 部署所有模組
- `deploy <module>` - 部署指定模組
- `deploy rollback <module>` - 回滾指定模組

### 環境設定
- `setup init` - 初始化開發環境
- `setup check` - 檢查環境依賴
- `setup config` - 編輯配置

## 📋 可用模組

- `core` - zientis-core (核心模組)
- `economy` - zientis-economy (經濟系統)
- `social` - zientis-social (社交系統)
- `nations` - zientis-nations (國家系統)
- `multiworld` - zientis-multiworld (多世界管理)
- `display` - zientis-display (顯示介面)
- `discord-api` - zientis-discord-api (Discord API)

## 🔧 常用選項

- `-v, --verbose` - 詳細輸出
- `-q, --quiet` - 安靜模式
- `-f, --force` - 強制執行
- `-c, --clean` - 清理後執行
- `--dry-run` - 乾跑模式

## 💡 使用範例

### 開發工作流程

**Windows**:
```cmd
REM 1. 啟動監控模式
zientis.bat build watch

REM 2. 編輯程式碼（工具會自動檢測變更並重新編譯）

REM 3. 如有問題，查看詳細日誌
zientis.bat build core --verbose

REM 4. 檢查部署狀態
zientis.bat deploy status

REM 5. 如需回滾
zientis.bat deploy rollback core
```

**Linux/macOS**:
```bash
# 1. 啟動監控模式
./zientis build watch

# 2. 編輯程式碼（工具會自動檢測變更並重新編譯）

# 3. 如有問題，查看詳細日誌
./zientis build core --verbose

# 4. 檢查部署狀態
./zientis deploy status

# 5. 如需回滾
./zientis deploy rollback core
```

## 🌟 特色功能

- **🔄 智慧回退** - Kotlin 未安裝時自動使用替代方案
- **🎨 彩色輸出** - 清晰的視覺回饋
- **📋 詳細幫助** - 每個命令都有完整說明
- **🌍 跨平台** - 支援 Windows、Linux、macOS
- **⚡ 自動檢測** - 智慧選擇最適合的執行方式

## 📚 詳細文檔

更多詳細資訊請參考 [tools/README.md](tools/README.md)

## 🐛 故障排除

### 常見問題

1. **命令找不到**
   - Windows: 確認使用 `zientis.bat` 而非 `zientis`
   - Linux/macOS: 確認使用 `./zientis` 或建立符號鏈結

2. **編譯失敗**
   - 檢查 Java 和 Gradle 是否正確安裝
   - 使用 `--verbose` 選項查看詳細錯誤

3. **權限問題**
   - Linux/macOS: 執行 `chmod +x zientis` 確保可執行

### 獲得幫助

```bash
# 查看完整命令列表
./zientis help                # Linux/macOS
zientis.bat help             # Windows

# 查看特定命令幫助
./zientis build help         # Linux/macOS
zientis.bat build help       # Windows
```

---

**🎯 讓開發更簡單，讓部署更可靠！現在跨平台統一更方便！**