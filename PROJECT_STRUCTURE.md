# Zientis Minecraft 插件專案結構

## 專案概述

Zientis 是一個模組化的 Minecraft 插件系統，提供經濟、多世界、展示、國家和社交功能的集成解決方案。

## 根目錄文件

### 配置文件
- `build.gradle.kts` - Gradle 建構配置
- `settings.gradle.kts` - Gradle 設定文件
- `gradle.properties` - Gradle 屬性配置
- `docker-compose.dev.yml` - 開發環境 Docker 配置

### 文檔文件
- `README.md` - 專案主要說明文檔
- `LICENSE` - 授權許可文檔
- `ROADMAP.md` - 專案發展路線圖
- `DEPLOYMENT.md` - 部署指南
- `IMPLEMENTATION_PLAN.md` - 實作計劃
- `NEXT_DEVELOPMENT_PLAN.md` - 下一步開發計劃
- `DISCORD_*.md` - Discord 整合相關文檔

### 配置目錄
- `config/` - 專案配置文件
  - `development.yml` - 開發環境配置
- `config-examples/` - 配置範例文件
  - `discord-hybrid-config.yml` - Discord 混合配置範例
  - `discord-integration-config.yml` - Discord 整合配置範例

### 支援目錄
- `scripts/` - 開發和部署腳本
  - `dev-setup.sh` - 開發環境設置腳本
  - `test-server.sh` - 測試伺服器腳本
- `database/` - 資料庫相關文件
  - `init/01-schema.sql` - 資料庫初始化腳本

## 核心模組架構

### 1. zientis-core (核心模組)
**功能**: 提供基礎架構和共用服務

**主要組件**:
- `ZientisCore.java` - 核心系統主類別
- `ZientisCorePlugin.java` - Bukkit 插件入口點
- `api/ZientisAPI.java` - 對外 API 介面

**服務層**:
- `service/` - 服務管理框架
  - `AbstractService.java` - 抽象服務基類
  - `ServiceManager.java` - 服務管理器
  - `ServiceRegistry.java` - 服務註冊表
- `injection/` - 依賴注入容器
  - `DependencyContainer.java` - 依賴容器
  - `Injectable.java` - 可注入介面

**配置系統**:
- `config/` - 配置管理
  - `ConfigManager.java` - 配置管理器
  - `ConfigService.java` - 配置服務
  - `ZientisConfig.java` - 核心配置類別

**資料存取層**:
- `database/` - 資料庫管理
  - `DatabaseManager.java` - 資料庫管理器
  - `DatabaseConfig.java` - 資料庫配置
  - `DatabaseUtils.java` - 資料庫工具類別

**Discord 整合**:
- `discord/` - Discord 整合服務
  - `DiscordIntegrationService.java` - Discord 整合服務
  - `DiscordApiClient.java` - Discord API 客戶端
  - `DirectDiscordApiClient.java` - 直接 Discord API 客戶端
  - `DiscordSRVService.java` - DiscordSRV 服務

**Web 服務**:
- `web/WebServerManager.java` - Web 伺服器管理器

### 2. zientis-economy (經濟模組)
**功能**: 虛擬經濟系統，支援餘額管理和交易

**核心組件**:
- `ZientisEconomyPlugin.java` - 經濟插件主類別
- `api/ZientisEconomyAPI.java` - 經濟 API
- `manager/EconomyManager.java` - 經濟管理器

**資料模型**:
- `data/` - 資料模型
  - `EconomyAccount.java` - 經濟帳戶
  - `Transaction.java` - 交易記錄

**命令系統**:
- `commands/` - 玩家命令
  - `BalanceCommand.java` - 餘額查詢命令
  - `PayCommand.java` - 轉帳命令
  - `EconomyCommand.java` - 經濟管理命令

**整合功能**:
- `vault/ZientisVaultEconomy.java` - Vault 經濟整合
- `discord/DiscordEconomyData.java` - Discord 經濟資料
- `listener/EconomyDiscordListener.java` - Discord 經濟監聽器
- `listeners/EconomyEventListener.java` - 遊戲事件監聽器

**工具類別**:
- `util/JsonBuilder.java` - JSON 建構工具

### 3. zientis-multiworld (多世界模組)
**功能**: 島嶼世界管理和備份系統

**核心組件**:
- `ZientisMultiWorldPlugin.java` - 多世界插件主類別
- `api/ZientisMultiWorldAPI.java` - 多世界 API
- `manager/WorldManager.java` - 世界管理器

**備份系統**:
- `backup/BackupManager.java` - 備份管理器
- `backups/` - 備份文件儲存目錄

**功能模組**:
- `commands/IslandCommand.java` - 島嶼管理命令
- `listeners/PlayerJoinListener.java` - 玩家加入監聽器
- `discord/DiscordIslandData.java` - Discord 島嶼資料

### 4. zientis-display (展示模組)
**功能**: 3D 展示和全息圖系統

**核心組件**:
- `ZientisDisplayPlugin.java` - 展示插件主類別
- `api/` - 展示 API
  - `ZientisDisplayAPI.java` - 展示 API 介面
  - `ZientisDisplayAPIImpl.java` - 展示 API 實作

**資料模型**:
- `data/` - 展示資料模型
  - `DisplayModel.java` - 展示模型
  - `BlockPosition.java` - 方塊位置
  - `BoundingBox.java` - 邊界盒
  - `HologramData.java` - 全息圖資料
  - `ParticleEffectData.java` - 粒子效果資料

**渲染引擎**:
- `renderer/` - 渲染器
  - `DisplayRenderer.java` - 展示渲染器
  - `HologramRenderer.java` - 全息圖渲染器
  - `ParticleRenderer.java` - 粒子渲染器
- `engine/` - 引擎系統
  - `BlockMappingEngine.java` - 方塊映射引擎
  - `ScalingEngine.java` - 縮放引擎

**管理系統**:
- `manager/DisplayRegionManager.java` - 展示區域管理器
- `tasks/DisplayUpdateTask.java` - 展示更新任務
- `listeners/DisplayInteractionListener.java` - 展示互動監聽器

### 5. zientis-nations (國家模組)
**功能**: 玩家國家和外交系統

**核心組件**:
- `ZientisNationsPlugin.java` - 國家插件主類別
- `api/ZientisNationsAPI.java` - 國家 API

**資料模型**:
- `data/` - 國家資料模型
  - `Nation.java` - 國家
  - `NationLevel.java` - 國家等級
  - `NationFlag.java` - 國家旗幟
  - `NationPermission.java` - 國家權限
  - `DiplomaticRelation.java` - 外交關係
  - `DiscordNationData.java` - Discord 國家資料

### 6. zientis-social (社交模組)
**功能**: 社交互動系統（開發中）

**狀態**: 基礎架構已建立，功能開發中

### 7. zientis-discord-api (Discord API 模組)
**功能**: Discord 整合 API 服務

**核心組件**:
- `ZientisDiscordApiPlugin.java` - Discord API 插件主類別
- `api/ZientisDiscordAPI.java` - Discord API 介面

**控制器**:
- `controller/DiscordEconomyController.java` - Discord 經濟控制器

**資料傳輸**:
- `dto/` - 資料傳輸物件
  - `SyncRequest.java` - 同步請求
  - `SyncResponse.java` - 同步回應

**安全和服務**:
- `security/DiscordAuthService.java` - Discord 認證服務
- `service/UserMappingService.java` - 使用者映射服務

## Minecraft 伺服器環境

### minecraft-server/ 目錄
- `paper-1.20.6.jar` - Paper 伺服器核心
- `plugins/` - 插件目錄
  - `zientis-economy-0.1.0-ALPHA.jar` - 已部署的經濟插件
  - `zientis-multiworld-0.1.0-ALPHA.jar` - 已部署的多世界插件
- `world/` - 主世界資料
- `config/` - Paper 配置文件
- `logs/` - 伺服器日誌

## 建構系統

**Gradle 多模組專案**:
- 使用 Kotlin DSL (`build.gradle.kts`)
- 統一版本管理和依賴配置
- 支援並行建構和測試

**建構命令**:
```bash
./gradlew build          # 建構所有模組
./gradlew test           # 執行所有測試
./gradlew publishToMavenLocal  # 發佈到本地倉庫
```

## 測試架構

**每個模組都包含**:
- `src/test/java/` - 單元測試
- 對應的測試類別涵蓋核心功能
- 整合測試支援

**測試策略**:
- 單元測試：核心邏輯測試
- 整合測試：模組間互動測試
- API 測試：對外介面測試

## 部署和開發

**開發環境**:
- Docker Compose 支援本地開發
- 自動化測試伺服器設置
- 熱重載支援

**配置管理**:
- 環境分離（開發/生產）
- 配置範例提供
- Discord 整合配置支援

## 模組依賴關係

```
zientis-core (核心)
├── zientis-economy (依賴 core)
├── zientis-multiworld (依賴 core)
├── zientis-display (依賴 core)
├── zientis-nations (依賴 core)
├── zientis-social (依賴 core)
└── zientis-discord-api (依賴 core)
```

## 版本資訊

- **當前版本**: 0.1.0-ALPHA
- **支援 Minecraft 版本**: 1.20.6
- **支援伺服器**: Paper
- **Java 版本**: 17+

## 開發狀態

- ✅ **核心架構**: 完成
- ✅ **經濟系統**: 基礎功能完成
- ✅ **多世界系統**: 基礎功能完成
- ✅ **展示系統**: 功能完成
- 🚧 **國家系統**: 基礎架構完成
- 🚧 **社交系統**: 開發中
- ✅ **Discord 整合**: 基礎整合完成