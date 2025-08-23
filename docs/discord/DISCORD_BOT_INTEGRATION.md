# Discord Bot 整合架構設計

## 概覽

為Zientis服務器設計統一的Discord Bot整合系統，支援**混合架構**：
- **Bot Token直接串接** - DiscordSRV風格功能（聊天、狀態、事件）
- **經濟API串接** - 跨平台經濟數據同步和複雜業務邏輯

## 🎯 最新更新（2024年）

✅ **混合架構完成** - 同時支援Bot Token和經濟API連接  
✅ **DiscordSRV風格實作** - 完整的伺服器同步功能  
✅ **直接Discord API** - 原生Discord REST API整合  
✅ **智能回退機制** - 多重連接保證服務不中斷

## 整體架構

### 核心模組結構
```
zientis-discord-api/
├── src/main/java/com/zientis/discord/
│   ├── api/               # Discord Bot API接口
│   ├── dto/               # 數據傳輸對象
│   ├── service/           # Discord服務實現
│   ├── webhook/           # Webhook處理器
│   ├── security/          # 安全認證
│   └── integration/       # 各系統整合
│       ├── economy/       # 經濟系統整合
│       ├── nations/       # 國家系統整合
│       ├── multiworld/    # 多世界系統整合
│       └── display/       # 展示系統整合
```

## API設計原則

### 1. 統一的響應格式
```json
{
  "status": "success|error",
  "message": "操作描述",
  "data": {},
  "timestamp": "2024-12-15T10:30:00Z",
  "server": "zientis-main"
}
```

### 2. 安全認證
- Discord OAuth2整合
- 玩家帳號綁定驗證
- 權限等級檢查
- API密鑰管理

### 3. 多語言支援
- 中文/英文自動切換
- 基於用戶偏好設置

## 各系統Discord Bot功能

### 🏦 經濟系統 (ZientisEconomy)
**Discord指令**:
- `/balance` - 查詢餘額
- `/pay @用戶 金額` - 轉帳給其他玩家
- `/economy stats` - 伺服器經濟統計
- `/transactions [數量]` - 查詢交易記錄

**Webhook事件**:
- 大額交易通知
- 經濟異常警報
- 每日/週經濟報告

### 🏝️ 多世界系統 (ZientisMultiWorld)
**Discord指令**:
- `/island info` - 查詢島嶼信息
- `/island list` - 列出可訪問島嶼
- `/island backup` - 手動備份島嶼
- `/island stats` - 島嶼統計信息

**Webhook事件**:
- 島嶼創建/刪除通知
- 備份完成通知
- 記憶體使用警報

### 🎭 展示系統 (ZientisDisplay)
**Discord指令**:
- `/display info [島嶼ID]` - 查詢展示信息
- `/display stats` - 展示系統統計
- `/display rank` - 島嶼排行榜

**Webhook事件**:
- 新展示創建通知
- 展示等級升級通知

### 🏛️ 國家系統 (ZientisNations)
**Discord指令**:
- `/nation info [國家名]` - 查詢國家信息
- `/nation members` - 查詢國家成員
- `/nation treasury` - 查詢國庫狀況
- `/nation diplomacy` - 外交狀態
- `/nation wars` - 戰爭狀態

**Webhook事件**:
- 國家創建/解散通知
- 戰爭宣告/結束通知
- 外交狀態變更
- 國庫重大變動

## API端點設計

### 基礎端點
```
GET  /api/v1/discord/server/status     # 服務器狀態
POST /api/v1/discord/auth/link         # 帳號綁定
GET  /api/v1/discord/user/{discordId}  # 用戶信息
```

### 經濟系統端點
```
GET  /api/v1/discord/economy/balance/{playerId}
POST /api/v1/discord/economy/transfer
GET  /api/v1/discord/economy/transactions/{playerId}
GET  /api/v1/discord/economy/stats
```

### 多世界系統端點
```
GET  /api/v1/discord/multiworld/islands/{playerId}
GET  /api/v1/discord/multiworld/island/{islandId}
POST /api/v1/discord/multiworld/backup/{islandId}
GET  /api/v1/discord/multiworld/stats
```

### 展示系統端點
```
GET  /api/v1/discord/display/info/{islandId}
GET  /api/v1/discord/display/rank
GET  /api/v1/discord/display/stats
```

### 國家系統端點
```
GET  /api/v1/discord/nations/info/{nationName}
GET  /api/v1/discord/nations/members/{nationId}
GET  /api/v1/discord/nations/treasury/{nationId}
GET  /api/v1/discord/nations/diplomacy/{nationId}
GET  /api/v1/discord/nations/wars
```

## 數據同步策略

### 實時同步
- 重要事件立即推送Discord
- 使用Webhook進行實時通知

### 定時同步
- 每小時統計數據更新
- 每日報告生成

### 緩存策略
- Redis緩存熱點數據
- 5分鐘緩存過期時間

## 安全考量

### 1. 認證機制
```java
@Component
public class DiscordAuthService {
    // Discord OAuth2 驗證
    // 玩家帳號綁定驗證
    // JWT令牌管理
}
```

### 2. 權限控制
```java
public enum DiscordPermission {
    VIEW_BASIC,      // 基礎查詢權限
    MANAGE_ECONOMY,  // 經濟管理權限
    ADMIN_COMMANDS,  // 管理員指令權限
    SYSTEM_ALERTS    // 系統警報權限
}
```

### 3. 速率限制
- 每用戶每分鐘最多20次API調用
- 管理員指令額外限制

## 錯誤處理

### 標準錯誤碼
```java
public enum DiscordApiError {
    PLAYER_NOT_FOUND(1001, "玩家不存在"),
    INSUFFICIENT_BALANCE(2001, "餘額不足"),
    ISLAND_NOT_ACCESSIBLE(3001, "島嶼無法訪問"),
    NATION_NOT_FOUND(4001, "國家不存在"),
    PERMISSION_DENIED(5001, "權限不足"),
    RATE_LIMIT_EXCEEDED(5002, "請求過於頻繁");
}
```

## 監控與日誌

### 監控指標
- API調用次數統計
- 響應時間監控
- 錯誤率統計
- 用戶活躍度分析

### 日誌記錄
- 所有API調用記錄
- 安全事件記錄
- 錯誤詳細日誌

## 實施階段

### Phase 1: 基礎架構
1. 創建discord-api模組
2. 實現基礎認證系統
3. 設計統一API格式

### Phase 2: 核心系統整合
1. 經濟系統API
2. 多世界系統API
3. 展示系統API

### Phase 3: 高級功能
1. 國家系統API
2. Webhook事件系統
3. 實時通知功能

### Phase 4: 優化與監控
1. 性能優化
2. 監控系統
3. 安全加固

## 配置示例

```yaml
discord:
  api:
    enabled: true
    base-url: "https://api.zientis.com"
    webhook-url: "${DISCORD_WEBHOOK_URL}"
    
  auth:
    client-id: "${DISCORD_CLIENT_ID}"
    client-secret: "${DISCORD_CLIENT_SECRET}"
    redirect-uri: "https://auth.zientis.com/discord/callback"
    
  rate-limit:
    requests-per-minute: 20
    admin-requests-per-minute: 100
    
  features:
    economy: true
    multiworld: true
    display: true
    nations: true
```

此架構設計確保了各系統與Discord Bot的無縫整合，同時保持了系統的安全性和可擴展性。