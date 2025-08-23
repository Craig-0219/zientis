# Zientis Discord Bot Token 整合指南

## 🎯 概述

Zientis現在支援**混合架構**的Discord整合，您可以同時使用：

1. **Bot Token直接串接** - 用於DiscordSRV風格功能（聊天、狀態、事件）
2. **經濟API串接** - 用於跨平台經濟數據同步和複雜業務邏輯

## 🔧 設定步驟

### 步驟 1: 創建Discord Bot

1. 前往 [Discord Developer Portal](https://discord.com/developers/applications)
2. 點擊 "New Application" 創建新應用
3. 在左側選擇 "Bot"
4. 點擊 "Add Bot" 創建機器人
5. 複製 **Token**（這就是您的Bot Token）

### 步驟 2: 設定Bot權限

在Bot設定頁面中，啟用以下權限：

**Bot Permissions:**
- ✅ Send Messages（發送訊息）
- ✅ Embed Links（嵌入連結）
- ✅ Attach Files（附加檔案）
- ✅ Read Message History（讀取訊息歷史）
- ✅ Manage Messages（管理訊息）
- ✅ Manage Channels（管理頻道）
- ✅ Use External Emojis（使用外部表情符號）

**Privileged Gateway Intents:**
- ✅ Message Content Intent（訊息內容意圖）

### 步驟 3: 邀請Bot到伺服器

1. 在 "OAuth2" → "URL Generator" 中：
   - 選擇 "bot" scope
   - 選擇上述權限
2. 複製生成的URL並在瀏覽器中開啟
3. 選擇您的Discord伺服器並邀請Bot

### 步驟 4: 獲取必要ID

**伺服器ID (Guild ID):**
1. 右鍵點擊Discord伺服器名稱
2. 選擇 "複製ID"

**頻道ID:**
1. 右鍵點擊頻道名稱
2. 選擇 "複製ID"

### 步驟 5: 配置Minecraft插件

使用 `discord-hybrid-config.yml` 範例配置：

```yaml
discord:
  enabled: true
  connection_mode: "HYBRID"  # 混合模式
  
  # Bot Token設定
  bot_token: "你的Bot Token"
  guild_id: "你的Discord伺服器ID" 
  use_direct_api: true
  
  # 經濟API設定（可選）
  bot_api_endpoint: "http://localhost:8080/api/v1"
  api_key: "你的經濟API金鑰"
  use_economy_api: true
  
  # 頻道設定
  chat_channel_id: "聊天頻道ID"
  status_channel_id: "狀態頻道ID"
  log_channel_id: "日誌頻道ID"
  
  # 功能啟用
  chat_sync: true
  server_status_embed: true
  join_leave_messages: true
  death_messages: true
  achievement_messages: true
```

## 🚀 功能特色

### Bot Token直接串接功能

✅ **即時聊天同步**
- Minecraft聊天 → Discord頻道
- Discord頻道 → Minecraft遊戲內
- 支援玩家頭像顯示（透過Webhook）

✅ **伺服器狀態嵌入**
- 即時在線玩家數
- TPS監控和狀態指示
- 在線玩家列表
- 自動定期更新

✅ **事件通知**
- 玩家加入/離開通知
- 玩家死亡訊息
- 成就解鎖通知
- 伺服器啟動/關閉通知

✅ **頻道管理**
- 自動更新頻道話題
- 支援多頻道配置
- 訊息編輯和管理

### 經濟API整合功能

✅ **跨平台經濟同步**
- 經濟交易事件通知
- 玩家經濟數據同步
- 成就系統整合

✅ **Webhook美觀顯示**
- 玩家頭像顯示
- 自定義使用者名稱
- 豐富的訊息格式

## 🔄 連接模式說明

### HYBRID模式（推薦）
```yaml
connection_mode: "HYBRID"
use_direct_api: true
use_economy_api: true
```
- DiscordSRV功能使用Bot Token直接串接
- 經濟系統使用API串接
- 最佳性能和功能完整性

### BOT_TOKEN_ONLY模式
```yaml
connection_mode: "BOT_TOKEN_ONLY"
use_direct_api: true
use_economy_api: false
```
- 僅使用Bot Token
- 所有功能通過Discord REST API實現
- 簡單配置，無需額外API服務器

### ECONOMY_API_ONLY模式
```yaml
connection_mode: "ECONOMY_API_ONLY"
use_direct_api: false
use_economy_api: true
```
- 僅使用經濟API
- 需要中介API服務器
- 適合複雜的跨平台整合

## 🛠️ 故障排除

### 常見問題

**1. Bot無回應**
- 檢查Bot Token是否正確
- 確認Bot已邀請到伺服器
- 檢查Bot權限是否足夠

**2. 無法發送訊息**
- 確認頻道ID正確
- 檢查Bot是否有該頻道的發送權限
- 查看控制台錯誤訊息

**3. 狀態嵌入不更新**
- 檢查status_channel_id設定
- 確認更新間隔設定合理
- 查看TPS獲取是否正常

### 除錯建議

**啟用除錯模式:**
```yaml
debug: true
logging:
  level: "DEBUG"
```

**查看日誌:**
```bash
tail -f logs/zientis.log
```

**測試連接:**
- 重新載入插件後檢查Discord連接日誌
- 使用 `/zientis discord status` 命令檢查狀態

## 📝 最佳實務

### 安全考量
- 妥善保管Bot Token，不要分享給他人
- 使用環境變數或安全的配置管理
- 定期檢查Bot權限

### 性能最佳化
- 合理設定更新間隔避免API限制
- 使用HYBRID模式獲得最佳性能
- 監控Discord API使用量

### 用戶體驗
- 設定合適的頻道分類
- 配置適當的訊息過濾
- 測試各種功能確保正常運作

## 🎉 部署完成

配置完成後，您將看到：

1. **Discord頻道中出現機器人在線**
2. **伺服器狀態嵌入訊息開始更新**
3. **Minecraft聊天同步到Discord**
4. **玩家事件通知正常發送**

享受您的Zientis Discord整合體驗！🚀

---

> 💡 **提示**: 如果遇到問題，請查看 [故障排除指南](./TROUBLESHOOTING.md) 或在社群中尋求幫助。