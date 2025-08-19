package com.zientis.core.discord;

import com.zientis.core.discord.model.CrossPlatformUser;
import com.zientis.core.discord.model.GameEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Discord整合功能測試
 */
class DiscordIntegrationTest {
    
    @Mock
    private Plugin mockPlugin;
    
    private DiscordConfig discordConfig;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockPlugin.getDataFolder()).thenReturn(new java.io.File("test-data"));
        when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));
        when(mockPlugin.getServer()).thenReturn(org.mockito.Mockito.mock(org.bukkit.Server.class));
        
        // 建立測試配置
        discordConfig = new DiscordConfig()
                .setEnabled(true)
                .setBotApiEndpoint("http://localhost:8080/api/v1")
                .setApiKey("test-api-key")
                .setServerKey("test-server-key");
    }
    
    @Test
    void testDiscordConfigDefaults() {
        DiscordConfig config = new DiscordConfig();
        
        assertEquals("http://localhost:8080/api/v1", config.getBotApiEndpoint());
        assertEquals("", config.getApiKey());
        assertFalse(config.isEnabled());
        assertTrue(config.isEconomySync());
        assertTrue(config.isAchievementSync());
        assertEquals(300, config.getSyncInterval());
    }
    
    @Test
    void testDiscordConfigChaining() {
        DiscordConfig config = new DiscordConfig()
                .setBotApiEndpoint("http://test.com")
                .setApiKey("test-key")
                .setEnabled(true)
                .setEconomySync(false)
                .setConnectionTimeout(60000);
        
        assertEquals("http://test.com", config.getBotApiEndpoint());
        assertEquals("test-key", config.getApiKey());
        assertTrue(config.isEnabled());
        assertFalse(config.isEconomySync());
        assertEquals(60000, config.getConnectionTimeout());
    }
    
    @Test
    void testCrossPlatformUserModel() {
        Long discordId = 123456789L;
        UUID minecraftUuid = UUID.randomUUID();
        String username = "TestPlayer";
        
        CrossPlatformUser user = new CrossPlatformUser(discordId, minecraftUuid, username);
        user.setTotalCoins(1000);
        user.setTotalGems(50);
        user.setTotalExperience(2500);
        user.setLevel(5);
        
        assertEquals(discordId, user.getDiscordId());
        assertEquals(minecraftUuid, user.getMinecraftUuid());
        assertEquals(username, user.getUsername());
        assertEquals(1000, user.getTotalCoins());
        assertEquals(50, user.getTotalGems());
        assertEquals(2500, user.getTotalExperience());
        assertEquals(5, user.getLevel());
        
        // 測試經濟資料轉換
        Map<String, Object> economyData = user.getEconomyData();
        assertEquals(1000, economyData.get("total_coins"));
        assertEquals(50, economyData.get("total_gems"));
        assertEquals(2500, economyData.get("total_experience"));
        assertEquals(5, economyData.get("level"));
        
        // 測試等級進度計算
        assertTrue(user.getLevelProgress() >= 0.0);
        assertTrue(user.getLevelProgress() <= 100.0);
    }
    
    @Test
    void testGameEventCreation() {
        String playerName = "TestPlayer";
        String playerId = UUID.randomUUID().toString();
        
        // 測試玩家加入事件
        GameEvent joinEvent = GameEvent.playerJoin(playerName, playerId);
        assertEquals(GameEvent.EventType.PLAYER_JOIN, joinEvent.getEventType());
        assertEquals(playerName, joinEvent.getPlayerName());
        assertEquals(playerId, joinEvent.getPlayerId());
        assertFalse(joinEvent.isUrgent());
        
        // 測試玩家成就事件
        GameEvent achievementEvent = GameEvent.playerAchievement(
            playerName, playerId, "First Steps", "第一次登入伺服器");
        assertEquals(GameEvent.EventType.PLAYER_ACHIEVEMENT, achievementEvent.getEventType());
        assertTrue(achievementEvent.isUrgent());
        assertEquals("First Steps", achievementEvent.getEventData().get("achievement_name"));
        
        // 測試API格式轉換
        Map<String, Object> apiData = achievementEvent.toApiFormat();
        assertTrue(apiData.containsKey("event_type"));
        assertTrue(apiData.containsKey("player_name"));
        assertTrue(apiData.containsKey("timestamp"));
        assertTrue(apiData.containsKey("urgent"));
        assertTrue(apiData.containsKey("event_data"));
    }
    
    @Test
    void testDiscordIntegrationServiceCreation() {
        // 測試停用狀態
        DiscordConfig disabledConfig = new DiscordConfig().setEnabled(false);
        
        assertDoesNotThrow(() -> {
            DiscordIntegrationService service = new DiscordIntegrationService(mockPlugin, disabledConfig);
            assertEquals("DiscordIntegrationService", service.getName());
            assertEquals("1.0.0", service.getVersion());
            assertFalse(service.isRunning());
        });
    }
    
    @Test
    void testEconomyTransactionEvent() {
        String playerName = "TestPlayer";
        String playerId = UUID.randomUUID().toString();
        
        GameEvent economyEvent = GameEvent.economyTransaction(
            playerName, playerId, "earned", 100, "coins");
        
        assertEquals(GameEvent.EventType.ECONOMY_TRANSACTION, economyEvent.getEventType());
        assertEquals("earned", economyEvent.getEventData().get("transaction_type"));
        assertEquals(100, economyEvent.getEventData().get("amount"));
        assertEquals("coins", economyEvent.getEventData().get("currency"));
        
        Map<String, Object> apiFormat = economyEvent.toApiFormat();
        assertNotNull(apiFormat.get("event_data"));
    }
    
    @Test
    void testServerStatusEvents() {
        String serverName = "TestServer";
        
        GameEvent startEvent = GameEvent.serverStart(serverName);
        assertEquals(GameEvent.EventType.SERVER_START, startEvent.getEventType());
        assertEquals(serverName, startEvent.getServerName());
        assertTrue(startEvent.isUrgent());
        
        GameEvent stopEvent = GameEvent.serverStop(serverName);
        assertEquals(GameEvent.EventType.SERVER_STOP, stopEvent.getEventType());
        assertEquals(serverName, stopEvent.getServerName());
        assertTrue(stopEvent.isUrgent());
    }
}