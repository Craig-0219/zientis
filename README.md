# 🌌 賽恩堤斯 (Zientis) 伺服器計畫
## 革命性的Minecraft空島MMO伺服器

[![開發狀態](https://img.shields.io/badge/開發狀態-Stage%202%20完成-green)](https://github.com/craig900219/zientis)
[![Minecraft版本](https://img.shields.io/badge/Minecraft-1.20.6+-brightgreen)](https://papermc.io/)
[![Java版本](https://img.shields.io/badge/Java-21+-orange)](https://adoptium.net/)
[![構建工具](https://img.shields.io/badge/Build-Gradle%208.7-blue)](https://gradle.org/)
[![授權](https://img.shields.io/badge/授權-MIT-blue)](LICENSE)

---

## 📖 專案概述

賽恩堤斯是一個創新的Minecraft空島伺服器，結合了科技發展、國家建設、戰爭競爭的MMO元素。玩家在末世後的海洋世界中重建文明，每個島嶼都擁有獨立的世界，並在主世界中以3D微縮模型展示，創造出類似《Clash of Kings》的視覺體驗。

### 🎯 核心特色

- **🏝️ 一島一世界**：每個玩家島嶼都擁有完全獨立的世界實例
- **🎮 3D島嶼展示**：主世界中的微縮島嶼模型展示系統
- **🏛️ 國家系統**：玩家可組建國家，進行外交與戰爭
- **💰 完整經濟**：虛擬貨幣、市場交易、投資系統
- **⚔️ 戰爭機制**：國家間的領土爭奪與資源掠奪
- **🔬 科技樹**：基於Slimefun的深度科技發展系統

---

## 🏗️ 系統架構

### 核心系統模組

```
賽恩堤斯伺服器
├── 🌍 多世界系統 (ZientisMultiWorld)
│   ├── 世界生命周期管理
│   ├── 智能加載/卸載機制
│   └── 備份與災難恢復
│
├── 💰 經濟系統 (ZientisEconomy)
│   ├── 虛擬貨幣管理
│   ├── 全球市場系統
│   └── 投資與金融工具
│
├── 🏝️ 島嶼展示系統 (ZientisDisplay)
│   ├── 微縮模型生成器
│   ├── 全息信息系統
│   └── 互動與傳送機制
│
├── 🏛️ 國家戰爭系統 (ZientisNations)
│   ├── 國家管理與外交
│   ├── 領土控制機制
│   └── 戰爭與和平系統
│
└── 👥 社交系統 (ZientisSocial)
    ├── 好友與聯盟
    ├── 社交動態
    └── 社區功能
```

### 技術棧

- **核心平台**：Paper 1.20.6+
- **基礎框架**：BentoBox (空島管理)
- **科技系統**：Slimefun (科技樹)
- **資料庫**：MariaDB (主要數據)
- **快取系統**：Redis (性能優化)
- **外部整合**：Discord Bot

---

## 📅 開發路線圖

### 🎯 預計總開發時間：**8-10個月**

基於單人開發的實際情況，採用漸進式開發與發布策略。

---

## 🚀 第一階段：基礎架構 (3個月)

### 📋 **Version 0.1 Alpha** - 多世界系統

**開發時間：4-5週**

#### 功能目標
- ✅ 穩定的一島一世界架構
- ✅ 世界自動創建與銷毀
- ✅ 智能記憶體管理
- ✅ 基礎備份系統

#### 技術實現
```java
// 核心API設計
public interface ZientisMultiWorldAPI {
    CompletableFuture<World> createIslandWorld(UUID playerId);
    CompletableFuture<Boolean> deleteIslandWorld(UUID islandId);
    World getOrLoadWorld(UUID islandId);
    void scheduleWorldUnload(UUID islandId, long delay);
}
```

#### 里程碑驗收標準
- [ ] 能穩定創建100+個獨立世界
- [ ] 記憶體使用控制在8GB以內
- [ ] 世界切換延遲 < 3秒
- [ ] 備份恢復成功率 > 99%

---

### 📋 **Version 0.2 Alpha** - 經濟系統

**開發時間：3-4週**

#### 功能目標
- ✅ Vault API整合
- ✅ 基礎虛擬貨幣系統
- ✅ 玩家間轉帳功能
- ✅ 交易記錄與統計

#### 核心功能
```java
public class ZientisEconomyManager {
    // 基礎經濟操作
    public double getBalance(UUID playerId);
    public boolean transfer(UUID from, UUID to, double amount);
    
    // 交易系統
    public TransactionResult createTransaction(Transaction tx);
    public List<Transaction> getHistory(UUID playerId);
}
```

#### 里程碑驗收標準
- [ ] 支援基礎的經濟操作
- [ ] 交易記錄完整準確
- [ ] 與現有插件相容
- [ ] 通膨控制機制運作

---

### 📋 **Version 0.3 Alpha** - 島嶼展示核心

**開發時間：4-5週**

#### 功能目標
- ✅ 1:8比例微縮島嶼模型
- ✅ 基礎全息信息系統
- ✅ 右鍵互動功能
- ✅ 等級差異化展示

#### 視覺效果範例
```
新手島嶼 (等級1-10):
🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊
🌊🌊     🟤🟤🟤🟤🟤🟤🟤     🌊🌊
🌊🌊   🟤🟫🟫🏠🟫🟫🟫🟤   🌊🌊
🌊🌊 🟤🟫🟫🏠🟫🔥🟫🟫🟤 🌊🌊
🌊🌊 🟤🟫🌾🌾🌾🌾🟫🟫🟤 🌊🌊
🌊🌊     🟤🟤🟤🟤🟤🟤🟤     🌊🌊
🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊🌊

特徵：🏠 簡單木屋、🔥 營火效果、🌾 基礎農田
```

#### 里程碑驗收標準
- [ ] 能正確展示島嶼建築
- [ ] 全息信息即時更新
- [ ] 支援100+島嶼同時展示
- [ ] 視覺效果流暢不卡頓

---

## 🏛️ 第二階段：核心系統 (2.5個月)

### 📋 **Version 0.4 Beta** - 國家系統

**開發時間：3-4週**

#### 功能目標
- ✅ 國家創建與管理
- ✅ 成員邀請與權限
- ✅ 國庫經濟系統
- ✅ 基礎外交功能

#### 系統設計
```java
public class Nation {
    private UUID nationId;
    private String nationName;
    private UUID founderId;
    private Set<UUID> members;
    private Set<UUID> territories;
    private double treasury;
    private NationSettings settings;
}
```

#### 里程碑驗收標準
- [ ] 支援國家創建與解散
- [ ] 國庫系統正常運作
- [ ] 成員權限管理完善
- [ ] 外交狀態正確維護

---

### 📋 **Version 0.5 Beta** - 主世界完善

**開發時間：2-3週**

#### 功能目標
- ✅ 主世界區域劃分
- ✅ 傳送系統完善
- ✅ 導航與GUI系統
- ✅ 社交互動功能

#### 主世界佈局
```
             北方高級區域 (等級50+)
    ╔══════════════════════════════════════╗
    ║  💜    💜    🌟💜🌟    💜    💜  ║
    ╚══════════════════════════════════════╝
                        |
                  ╔═══════════╗
                  ║ 🏛️ 中央廣場 ║
                  ║   🎯🏪📊   ║
                  ╚═══════════╝
                        |
        ╔═════════════════════════════╗
        ║      新手區域 (等級1-19)      ║
        ║   🟢    🟡    🟡    🟢      ║
        ╚═════════════════════════════╝
```

---

### 📋 **Version 0.6 Beta** - 戰爭系統

**開發時間：3-4週**

#### 功能目標
- ✅ 戰爭宣告與和平
- ✅ 領土爭奪機制
- ✅ 戰爭經濟系統
- ✅ 勝利條件判定

#### 里程碑驗收標準
- [ ] 戰爭流程完整運作
- [ ] 資源掠奪機制平衡
- [ ] 戰爭成本合理設定
- [ ] 和平協議系統穩定

---

## 🎮 第三階段：體驗優化 (2.5個月)

### 📋 **Version 0.7 RC** - 視覺增強

**開發時間：3週**

#### 功能目標
- ✅ 粒子效果系統
- ✅ 音效配套
- ✅ 動畫與特效
- ✅ 主題化展示

#### 特效系統
```java
public class ParticleEffectManager {
    // 島嶼主題效果
    public void applyIndustrialEffects(Location center);  // 🏭 工業煙霧
    public void applyTechEffects(Location center);        // ⚡ 能量環
    public void applyNatureEffects(Location center);      // 🌿 自然粒子
}
```

---

### 📋 **Version 0.8 RC** - 社交完善

**開發時間：2週**

#### 功能目標
- ✅ 好友系統
- ✅ 社交動態
- ✅ 訪問權限管理
- ✅ 社區活動

---

### 📋 **Version 0.9 RC** - 性能優化

**開發時間：2-3週**

#### 功能目標
- ✅ 距離分級渲染
- ✅ 智能更新調度
- ✅ 記憶體優化
- ✅ 監控系統

---

### 📋 **Version 1.0 Release** - 正式發布

**開發時間：2週**

#### 功能目標
- ✅ 完整功能整合
- ✅ 穩定性測試
- ✅ 文檔完善
- ✅ 玩家教學系統

---

## 📊 階段性發布計劃

### 🔥 **Alpha測試階段** (前3個月)
**目標玩家：**內部測試 + 核心玩家 (10-20人)

**開放功能：**
- ✅ 基礎多世界系統
- ✅ 簡單經濟交易
- ✅ 島嶼展示觀看

**測試重點：**
- 系統穩定性驗證
- 性能基準測試
- 核心功能驗證

### 🚀 **Beta測試階段** (4-6個月)
**目標玩家：**公開測試 (50-100人)

**開放功能：**
- ✅ 完整國家系統
- ✅ 戰爭與外交
- ✅ 進階經濟功能

**測試重點：**
- 遊戲平衡調整
- 用戶體驗優化
- 社區功能測試

### 🎉 **正式發布** (8-10個月後)
**目標玩家：**全面開放 (200+人)

**完整功能：**
- ✅ 所有核心系統
- ✅ 豐富視覺效果
- ✅ 完善社交功能

---

## 🛠️ 開發環境設置

### 系統需求

#### 開發環境
- **Java**: OpenJDK 21+
- **IDE**: IntelliJ IDEA / Eclipse
- **Build Tool**: Gradle 8.7+ 
- **Database**: MariaDB 10.6+
- **Cache**: Redis 6.0+
- **Testing**: JUnit 5 + Mockito

#### 伺服器需求
- **RAM**: 16GB+ (推薦32GB)
- **CPU**: 8核心+ (推薦16核心)
- **Storage**: NVMe SSD 500GB+
- **Network**: 千兆網路

### 依賴插件

```kotlin
dependencies {
    // 核心框架
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    
    // 空島管理
    compileOnly("world.bentobox:bentobox:1.20.6")
    
    // 科技系統
    compileOnly("io.github.thebusybiscuit:slimefun4:RC-32")
    
    // 經濟API
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    
    // 測試框架
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}
```

### 專案結構

```
zientis-server/
├── zientis-core/           # 核心API與共用組件
├── zientis-multiworld/     # 多世界系統
├── zientis-economy/        # 經濟系統
├── zientis-display/        # 島嶼展示系統
├── zientis-nations/        # 國家戰爭系統
├── zientis-social/         # 社交系統
├── zientis-discord/        # Discord整合
└── zientis-web/           # 網頁控制台 (未來)
```

---

## 📈 性能基準

### 目標性能指標

| 指標 | 目標值 | 備註 |
|------|--------|------|
| **TPS** | > 19.5 | 在100人在線時 |
| **記憶體使用** | < 12GB | 包含200個載入世界 |
| **世界切換延遲** | < 2秒 | 99%的情況下 |
| **島嶼展示更新** | < 5秒 | 從修改到視覺更新 |
| **經濟交易處理** | < 100ms | 單筆交易處理時間 |

### 監控系統

```java
public class PerformanceMonitor {
    // 實時性能指標
    public TPSMetrics getCurrentTPS();
    public MemoryMetrics getMemoryUsage();
    public WorldMetrics getWorldStatistics();
    
    // 性能警報
    public void alertOnLowTPS(double threshold);
    public void alertOnHighMemory(double threshold);
}
```

---

## 🧪 測試策略

### 單元測試
- **覆蓋率目標**: > 80%
- **關鍵模組**: 經濟系統、世界管理
- **工具**: JUnit 5 + Mockito

### 整合測試
- **系統間整合**: 多世界 + 經濟
- **插件相容性**: BentoBox + Slimefun
- **資料庫整合**: MariaDB + Redis

### 性能測試
- **壓力測試**: 模擬200人同時在線
- **記憶體測試**: 長期運行穩定性
- **併發測試**: 多世界同時操作

### 用戶測試
- **Alpha**: 內部功能驗證
- **Beta**: 公開平衡測試
- **RC**: 最終穩定性確認

---

## 🚨 風險管理

### 技術風險

#### 🔴 高風險
- **多世界系統穩定性**
  - 風險：記憶體洩漏、世界損壞
  - 應對：漸進式開發、充分測試、完善備份

#### 🟡 中等風險
- **經濟系統平衡**
  - 風險：通膨、經濟崩潰
  - 應對：數學模型驗證、小規模測試

- **性能瓶頸**
  - 風險：TPS下降、延遲增加
  - 應對：性能監控、優化策略、硬體升級

#### 🟢 低風險
- **插件相容性**
  - 風險：版本衝突、API變更
  - 應對：版本鎖定、相容性測試

### 專案風險

#### 時程風險
- **預估**: 8-10個月完成
- **緩衝**: 每階段預留20%額外時間
- **應對**: 功能優先級排序、MVP策略

#### 資源風險
- **人力**: 單人開發的限制
- **應對**: 合理分配工作量、考慮外包關鍵模組

---

## 📚 文檔與教學

### 開發文檔
- **API Reference**: 完整的API文檔
- **Architecture Guide**: 系統架構說明
- **Development Setup**: 開發環境設置指南

### 玩家教學
- **新手指南**: 基礎功能介紹
- **進階教學**: 國家建設、戰爭策略
- **FAQ**: 常見問題解答

### 管理文檔
- **Server Setup**: 伺服器部署指南
- **Configuration**: 配置參數說明
- **Troubleshooting**: 問題排查手冊

---

## 🤝 社群與貢獻

### Discord社群
- **開發討論**: 技術交流與問題討論
- **測試反饋**: Bug回報與建議
- **玩家交流**: 遊戲心得與攻略分享

### 貢獻指南
雖然主要由單人開發，但歡迎社群貢獻：

#### 歡迎的貢獻類型
- 🐛 **Bug回報**: 詳細的問題描述與重現步驟
- 💡 **功能建議**: 有建設性的改進建議
- 📝 **文檔改進**: 錯誤修正與內容補充
- 🎨 **美術資源**: 材質包、音效、圖標設計

#### 貢獻流程
1. Fork 專案
2. 創建功能分支
3. 提交變更
4. 發起 Pull Request
5. 代碼審查與合併

---

## 📞 聯絡方式

### 專案維護者
- **開發者**: [雞毛]
- **Email**: [craig900219@gmail.com]
- **Discord**: [UxpV7Yr9V8]

### 緊急聯絡
- **技術問題**: GitHub Issues
- **安全問題**: 私信聯絡
- **合作洽談**: Email聯絡

---

## 📄 授權信息

本專案採用 MIT License 授權。

```
MIT License

Copyright (c) 2024 Zientis Project

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🎯 專案願景

賽恩堤斯致力於創造一個革命性的Minecraft空島體驗，讓玩家能夠：

- 🏗️ **建設夢想島嶼**：在完全獨立的世界中自由創造
- 🏛️ **參與國家建設**：與其他玩家合作建立強大國家
- ⚔️ **體驗史詩戰爭**：為榮譽與資源而戰
- 🎮 **享受視覺震撼**：前所未有的3D島嶼展示
- 🤝 **建立深度社交**：在共同的目標中結交朋友

我們相信，透過技術創新與遊戲設計的完美結合，賽恩堤斯將成為Minecraft伺服器歷史上的一個重要里程碑。

**讓我們一起重建賽恩堤斯，創造屬於我們的數位文明！** 🚀

---

*最後更新：2024年12月*
*版本：v0.1-planning*
